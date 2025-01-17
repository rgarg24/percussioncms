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
package com.percussion.cx.error;

import com.percussion.error.PSStandaloneException;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * This class is a generic client exception used when an error occurs during
 * content explorer handling (loading, persisting, creating etc).  This class
 * uses the <code>PSI18NTranslationKeyValues</code> to gets it's localized
 * messages.  All of the message therefore are localized because that's how
 * the <code>PSI18NTranslationKeyValues</code> is created.  The necessary
 * methods are overridden in this class to achieve
 * this and is noted in the methods doc.
 */
public class PSContentExplorerException extends PSStandaloneException
{

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    */
   public PSContentExplorerException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param singleMessage the sole of argument to use as the arguments in the
    *    error message
    */
   public PSContentExplorerException(int msgCode, String singleMessage)
   {
      super(msgCode,singleMessage);
   }

   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *    error message
    */
   public PSContentExplorerException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode,arrayArgs);
   }

   /**
    * @see #com.percussion.error.PSStandaloneException PSStandaloneException
    */
   public String getResourceBundleBaseName()
   {
      return STRING_BUNDLE_NAME;
   }

   /**
    * Not implemented.
    */
   public String getXmlNodeName()
   {
      return null;
   }

   /**
    * Overridden method.  Simply calls
    * {@link #getMessage() getMessage()} and ignores the argument.  The reason
    * for this is due to how the <code>PSI18NTranslationKeyValues</code> get's
    * created. The <code>PSI18NTranslationKeyValues</code> is created for one
    * <code>Locale</code> at a time based on the users session
    * {@link com.percussion.util.IPSHtmlParameters#SYS_LANG
    * IPSHtmlParameters.SYS_LANG}.  Therefore requesting messages in another
    * <code>Locale</code> is unnecessary.
    *
    * @param locale - ignored, may be <code>null</code>.
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    * @see #getMessage() getMessage()
    */
   public String getLocalizedMessage(Locale locale)
   {
      return getMessage();
   }

   /**
    * Overridden method.  Simply calls {@link #getMessage() getMessage()}
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    * @see #getLocalizedMessage(Locale) getLocalizedMessage(Locale)
    */
   public String getLocalizedMessage()
   {
      return getMessage();
   }

   /**
    * Returns the detail message of this exception.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getMessage()
   {
      return createMessage();
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @return The formatted message, never <code>null</code>. If the appropriate
    *    message cannot be created, a message is constructed from the msgCode
    *    and args and is returned.
    */
   private String createMessage()
   {
      Object[] arrayArgs = super.getErrorArguments();
      if (arrayArgs == null)
         arrayArgs = new Object[0];

      String msg = PSI18NTranslationKeyValues.getInstance().getTranslationValue(
         "" + super.getErrorCode());

      //Get the error code
      String codeStr = String.valueOf(super.getErrorCode());
      //If the error code and translated message are same means we
      //could not find the translation for the given error code, show the error
      //message as is.
      if(msg.equals(codeStr))
      {
         msg = "";
      }

      // is it a message that we can format?
      if(msg != null && msg.trim().length() != 0)
      {
         try
         {
            msg = MessageFormat.format(msg, arrayArgs);
         }
         catch (IllegalArgumentException e)
         {
            // some problem with formatting
            msg = null;
         }
      }
      // may not be ours or there was a problem formatting:
      if (msg == null || msg.trim().length() == 0)
      {
         String sArgs = "";
         String sep = "";

         for (int i = 0; i < arrayArgs.length; i++) {
            sArgs += sep + arrayArgs[i].toString();
            sep = "; ";
         }

         msg += String.valueOf(super.getErrorCode()) + ": " + sArgs;
      }

      return msg;
   }


   /**
    * The String bundle used with this Exception.
    */
   public static final String STRING_BUNDLE_NAME =
      "com.percussion.cx.error.PSContentExplorerErrorStringBundle.properties";

}


