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
package com.percussion.security;

import com.percussion.error.PSErrorManager;
import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;

import java.util.Locale;

/**
 * The PSDataEncryptionError class is used to report an encryption error.
 * 
 *
 * @author      Chad Loder
 * @version      1.0
 * @since      1.0
 */
public class PSDataEncryptionError extends PSLogError {
   
   /**
    * Report a data encryption exception
    *
    * @param      errorCode   the specific error code returned
    * @param   args the error arguments
    */
   public PSDataEncryptionError(   int errorCode, Object[] args )
   {
      super(0);
      m_errorCode = errorCode;
      m_errorArgs = args;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* the generic submessage first */
      msgs[0]   = new PSLogSubMessage(
                              IPSSecurityErrors.DATA_ENCRYPTION_ERROR_MSG,
                              PSErrorManager.getErrorText(
                                    IPSSecurityErrors.DATA_ENCRYPTION_ERROR_MSG,
                                    false,
                                    loc));

      /* use the errorCode/errorParams to format the second submessage */
      msgs[1]   = new PSLogSubMessage(
                              m_errorCode,
                              PSErrorManager.createMessage(   m_errorCode,
                                                            m_errorArgs,
                                                            loc));

      return msgs;
   }


   private int         m_errorCode;
   private Object[]   m_errorArgs;
}

