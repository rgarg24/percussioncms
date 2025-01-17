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

package com.percussion.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.util.Date;

/**
 * The PSBackEndLogWriter class implements logging to a back-end data store.
 * <p>
 * The log will be stored in the same back-end data store as the object
 * store. Log records are broken up into two tables. The main table,
 * pslog, has the following format:
 * <table border="1">
 *   <tr><th>Column</th><th>Data Type</th><th>Description</th></tr>
 *   <tr><td>log_id_high</td>
 *         <td>INT (PRIMARY KEY)</td>
 *         <td>More significant half of the unique id for this entry.</td></tr>
 *   <tr><td>log_id_low</td>
 *         <td>INT (PRIMARY KEY)</td>
 *         <td>Less significant half of the unique id for this entry.</td></tr>
 *   <tr><td>log_type</td>
 *         <td>TINYINT</td>
 *         <td>The base type of the log entry:
 *               <ul>
 *                  <li>1 - error</li>
 *                  <li>2 - server start</li>
 *                  <li>3 - server stop</li>
 *                  <li>4 - application start</li>
 *                  <li>5 - application stop</li>
 *                  <li>6 - application statistics</li>
 *                  <li>7 - basic user activity</li>
 *                  <li>8 - detailed user activity</li>
 *                  <li>9 - multiple request handlers</li>
 *               </ul></td></tr>
 *   <tr><td>log_appl</td>
 *         <td>INT</td>
 *         <td>The application id of the application which generated this
 *               log entry.</td></tr>
 * </table>
 * The secondary table, pslogdat, has the following   format:
 * <table border="1">
 *   <tr><td>log_id_high</td>
 *         <td>INT (PRIMARY KEY, FOREIGN KEY)</td>
 *         <td>More significant half of the unique id for this entry.</td></tr>
 *   <tr><td>log_id_low</td>
 *         <td>INT (PRIMARY KEY, FOREIGN KEY)</td>
 *         <td>Less significant half of the unique id for this entry.</td></tr>
 *   <tr><td>log_seq</td>
 *         <td>TINYINT (PRIMARY KEY)</td>
 *         <td>Submessage number</td></tr>
 *   <tr><td>log_subseq</td>
 *         <td>INT (PRIMARY KEY)</td>
 *         <td>Submessage fragment number (for split submessages)</td></tr>
 *   <tr><td>log_subt</td>
 *         <td>INT</td>
 *         <td>The sub-type of log entry.
 *               For errors, the error code is used as the sub-type.
 *               <p>
 *               For application statistics:
 *               <ul>
 *                  <li>1 - elapsed time</li>
 *                  <li>2 - events processed</li>
 *                  <li>3 - events pending</li>
 *                  <li>4 - events failed</li>
 *                  <li>5 - cache hits</li>
 *                  <li>6 - cache misses</li>
 *                  <li>7 - minimum event time</li>
 *                  <li>8 - maximum event time</li>
 *                  <li>9 - average event time</li>
 *               </ul>
 *               For basic user activity:
 *               <ul>
 *                  <li>1 - user  session ID</li>
 *                  <li>2 - requestor host address</li>
 *                  <li>3 - requestor user name</li>
 *                  <li>4 - requested URL</li>
 *               </ul>
 *               For detailed user activity:
 *               <ul>
 *                  <li>1 - user session ID</li>
 *                  <li>2 - POST request body</li>
 *                  <li>3 - submitted XML file</li>
 *                  <li>4 - number of rows selected</li>
 *                  <li>5 - number of rows inserted</li>
 *                  <li>6 - number of rows updated</li>
 *                  <li>7 - number of rows deleted</li>
 *                  <li>8 - number of rows skipped</li>
 *                  <li>9 - number of rows failed</li>
 *               </ul>
 *               For multiple request handlers:
 *               <ul>
 *                  <li>1 - user session ID</li>
 *                  <li>2 - application/dataset names</li>
 *               </ul></td></tr>
 *   <tr><td>log_data</td>
 *         <td>VARCHAR(255)</td>
 *         <td>The text associated with this log entry.</td></tr>
 * </table>
 * The following indexes have been defined on the tables for faster access
 * to the data:
 * <ul>
 *   <li>pslog(log_time) - orders log entries by time, allowing for
 *         chronological searches</li>
 *   <li>pslog(log_type) - orders log entries by type, allowing for
 *         searches by a particular type</li>
 *   <li>pslog(log_appl, log_time) - orders log entries by application and
 *         time. This allows for searches by application ordered
 *         chronologically.</li>
 *   <li>pslogdat(log_id, log_subt, log_seq) - orders log entries by log id
 *         sub-type, and sequence number. This allows searching by log id and
 *         ordering by sub-type and sequence number within the log id.</li>
 * </ul>
 *
 * @author       Tas Giakouminakis
 * @version   1.0
 * @since        1.0
 */
public class PSBackEndLogWriter implements IPSLogWriter
{
   /**
    *  Construct a back-end log writer. This is given package access with the
    *  intent that only the PSLogManager object will instantiate it.
    *  <p>
    *
    *  @throws ClassNotFoundException if the class specified byt he
    * <code>loggerClassname</code> property cannot be loaded
    *  @throws NamingException If the default datasource details cannot be
    * obtained.
    */
   PSBackEndLogWriter()
      
   {
      
   }  

   public boolean isOpen()
   {      
      return true;
   }

   public void close()
   {
            
   }

   public void write(PSLogInformation msg) throws IllegalStateException
   {
      if (!ms_log.isDebugEnabled())
         return;
      
      {
         PSLogSubMessage[] subMessages = msg.getSubMessages();
         for (int sequence = 0; sequence < subMessages.length; sequence++)
         {
            PSLogSubMessage subMessage = subMessages[sequence];
            String msgText = subMessage.getText();
            ms_log.debug(msgText);
         }
      }
   }

   public boolean open()
   {
      return true;
   }

   public void truncateLog(Date allBefore)
   {
            
   }
   
   private static final Logger ms_log = LogManager.getLogger(PSBackEndLogWriter.class);
   
}
