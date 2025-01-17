/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.rx.publisher.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.process.PSProcessAction;
import com.percussion.process.PSProcessStatus;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.PSDateFormatISO8601;
import com.percussion.util.PSStringTemplate;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Run a command before or after an edition. <br/>
 * <br>
 * Takes two parameters from the UI.</br> <br>
 * command - Required - The fully qualified command to be run.</br> <br/>
 * timeOut - The maximum time in seconds to wait for the command to finish, or
 * wait indefinite if it is zero. The task fails if the exit code of the command
 * is non-zero or if the command has not finished in time. If it is not
 * specified, the command will run asynchronously and its exit code will be
 * ignored.
 * 
 * 
 * @author dougrand
 */
public class PSEditionCommandTask implements IPSEditionTask
{

   private static final Logger log = LogManager.getLogger(PSEditionCommandTask.class);

   public TaskType getType()
   {
      return TaskType.PREANDPOSTEDITION;
   }

   public void perform(IPSEdition edition, IPSSite site, Date start_time,
         Date end_time, long jobid, long duration, boolean success,
         Map<String, String> params, IPSEditionTaskStatusCallback status)
      throws Exception
   {
      String command = params.get("command");
      if (StringUtils.isBlank(command))
      {
         throw new IllegalArgumentException("No command defined");
      }

      int timeOut = -1;
      String timeOutParm = params.get("timeOut");
      if (StringUtils.isNumeric(timeOutParm))
      {
         timeOut = Integer.parseInt(timeOutParm.trim()) * 1000;
      }

      PSDateFormatISO8601 fmt = new PSDateFormatISO8601();
      Map<String,String> map = new HashMap<>();
      addVar(map, "editionName", edition.getDisplayTitle());
      addVar(map, "siteName", site.getName());
      addVar(map, "sitePublishingPath", site.getRoot());
      addVar(map, "editionStartTime", fmt.format(start_time));
      if (end_time != null)
      {
         addVar(map, "editionEndTime", fmt.format(end_time));
      }
      addVar(map, "sitePublishedUrl", site.getBaseUrl());
      IPSPubStatus pubStatus = status.getCurrentPubStatus();
      
      map.put("successCount", Integer.toString(pubStatus.getDeliveredCount()));
      map.put("failureCount", Integer.toString(pubStatus.getFailedCount()));

      // replace '\' with '/' because '\' is a escape char for PSStringTemplate
      String cmdString = command.replace('\\', '/');

      // Expand the command string
      PSStringTemplate template = new PSStringTemplate(cmdString, "${", "}");
      String cmdstr = template.expand(map);
      String cmd[] = cmdstr.split(" ");
      // Run
      PSProcessAction action = new PSProcessAction(cmd, null, null);
      long procInitStart = new Date().getTime();
      long timeout = 1000 * 60 * 10; // 10 minutes
      while (action.getStatus() == PSProcessStatus.PROCESS_NOT_STARTED)
      {
         try
         {
            // If process does not start within 10 minutes then
            // it should be considered a failure.
            long current = new Date().getTime();
            if (action.getStatus() == PSProcessStatus.PROCESS_FAILED_TO_START
                  || current - procInitStart >= timeout)
            {
               throwRuntimeException(cmdString, action.getStdErrorText(), null);
            }
            // Wait a little while before checking again
            Thread.sleep(100);
         }
         catch (InterruptedException e1)
         {
            log.error(PSExceptionUtils.getMessageForLog(e1));
            Thread.currentThread().interrupt();
         }
      }

      if (timeOut > -1)
      {
         waitAndCheckForErrors(cmdString, action, timeOut);
      }
   }

   /**
    * Waits for the command to finish, then check the exit code to see if there
    * was a non zero return code or if there was another error.
    * 
    * @param cmdString - The command that was executed. assume not
    * <code>null</null>
    * @param action - the results from the command that was executed. may be
    * <code>null</null> or blank
    * @param timeOut - time to wait for command to finish, assumed >= 0, wait
    * Indefinitely if it is 0
    */
   private void waitAndCheckForErrors(String cmdString,
         PSProcessAction action, int timeOut)
   {
      String msg = null;

      int exitCode = action.waitFor(timeOut);

      switch (action.getStatus())
      {
         case PSProcessStatus.PROCESS_FINISHED:
            if (exitCode != 0)
            {
               msg = "exited with return code " + exitCode;
            }
            break;
         case PSProcessStatus.PROCESS_FAILED_TO_START:
            msg = "failed to start";
            break;
         case PSProcessStatus.PROCESS_INTERRUPTED:
            msg = "was interupted";
            break;
         case PSProcessStatus.PROCESS_NOT_STARTED:
            msg = "failed to start";
            break;
         case PSProcessStatus.PROCESS_STARTED:
            msg = "did not complete in " + timeOut + " seconds";
            break;
         default:
            msg = " : Unknown PSProcessStatus: " + action.getStatus();
            break;
      }

      if (msg != null)
      {
         msg = cmdString + " " + msg;

         throwRuntimeException(cmdString, action.getStdErrorText(), msg);
      }
   }

   /**
    * Throws a runtime exception withthe apropriate error message
    * 
    * @param cmdstr - The command that was executed. assume not
    * <null>null</null>
    * @param errorText - Error text is added to message if not
    * <code>null</null> or blank
    * @param addMessage - Additional error info is added to message if not
    * <code>null</null> or blank
    */
   private void throwRuntimeException(String cmdstr, String errorText,
         String addMessage)
   {

      String msg = "Command failed: " + cmdstr;

      if (StringUtils.isNotBlank(errorText))
      {
         msg += "\nError text: " + errorText;
      }

      if (StringUtils.isNotBlank(addMessage))
      {
         msg += "\n" + addMessage;
      }

      throw new RuntimeException(msg);
   }

   /**
    * Add a value to the command vars, do nothing if the value is
    * <code>null</code> or empty.
    * 
    * @param vars the variables, assumed never <code>null</code>.
    * @param name the name, assumed never <code>null</code> or empty.
    * @param value the value, may be <code>null</code> or empty.
    */
   private void addVar(Map<String, String> vars, String name, String value)
   {
      if (StringUtils.isBlank(value))
         return;
      vars.put(name, value);
   }

   public void init(IPSExtensionDef def, File codeRoot)
   {
      // No initialization
   }

}
