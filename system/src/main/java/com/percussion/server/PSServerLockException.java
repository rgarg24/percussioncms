/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.server;

import com.percussion.error.PSException;

/**
 * Exception class to indicate an exception occurred attemting to acquire a lock
 * on a server resource.
 */
public class PSServerLockException extends PSException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode the error string to load.
    *
    * @param singleArg the argument to use as the sole argument in the error 
    * message, may be <code>null</code> or empty.
    */
   public PSServerLockException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode the error string to load.
    *
    * @param arrayArgs the array of arguments to use as the arguments in the 
    * error message.  May be <code>null</code>.
    */
   public PSServerLockException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode the error string to load
    */
   public PSServerLockException(int msgCode)
   {
      super(msgCode);
   }
}