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
package com.percussion.utils.jsr170;


import com.percussion.utils.beans.PSPropertyWrapper;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import java.util.Map;

/**
 * This is an implementation of the JSR-170 property iterator
 * 
 * @author dougrand
 */
public class PSPropertyIterator extends PSItemIterator<Property>
   implements PropertyIterator
{
   /**
    * Ctor
    * @param things the map of properties, never <code>null</code>
    * @param filterpattern the filter pattern, may be <code>null</code>
    */
   public PSPropertyIterator(Map<String, Property> things, String filterpattern) {
      super(things, filterpattern);
   }

   public Property nextProperty()
   {
      Property p = next();
      
      if (p != null && p instanceof PSPropertyWrapper)
      {
         PSPropertyWrapper wrapper = (PSPropertyWrapper) p;
         try
         {
            wrapper.init();
         }
         catch(IllegalStateException e)
         {
            return nextProperty();
         }
      }
      
      return p;
   }
}