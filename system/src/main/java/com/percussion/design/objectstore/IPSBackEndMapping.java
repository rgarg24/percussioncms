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

package com.percussion.design.objectstore;


/**
 * The IPSBackEndMapping interface must be implemented by any class which
 * will be used as a back-end mapping in a PSDataMapping object.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSBackEndMapping extends IPSReplacementValue
{
   /**
    * Get the columns which must be selected from the back-end(s) in
    * order to use this mapping. The column name syntax is 
    * <code>back-end-table-alias.column-name</code>.
    *
    * @return     the columns which must be selected from the back-end(s)
    *             in order to use this mapping.  If there are no columns, then
    *             <code>null</code> is returned.
    */
   public abstract String[] getColumnsForSelect();
}
