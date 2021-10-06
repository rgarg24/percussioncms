/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.extension;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * A base class for Jexl based extensions. Extend this class to ensure that
 * your JEXL extensions implement the correct interface.
 * 
 * @author dougrand
 */
public class PSJexlUtilBase implements IPSJexlExpression
{
   public static final String  VELOCITY_LOGGER="velocity";
   public static final String LOG_ERROR_DEFAULT="Error in $rx.pageutils.{}: {}";
   public  static final Logger log = LogManager.getLogger(VELOCITY_LOGGER);

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // Do nothing
   }

}