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

package com.percussion.search;

import com.percussion.extension.IPSExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;

import java.util.List;

/**
 * This interface is implemented by extensions capable of processing the search
 * result rows.
 * <p>
 * The search handler will construct an instance of this class and run it to
 * post process the search results that were generated by the search handler.
 * The implementing class can modify column values for each search result row.
 * It can add additional row(s) by cloning row(s). However,
 * <ul>
 * <li>it cannot add or delete columns to a row</li>
 * <li>it cannot add a new row without cloning an existing one.</li>
 * </ul>
 * <p>
 * <em>The implementation of this extension must be thread safe.</em>
 */
public interface IPSSearchResultsProcessor extends IPSExtension
{
   /**
    * The actual implementation of the row processing as described in the class
    * description.
    * 
    * @param params parameters to the extension as documented in the definition
    *           of the extension. Never <code>null</code>, may be empty.
    * @param rows a list of the search result
    *           {@link com.percussion.search.IPSSearchResultRow rows}, These
    *           are the rows the implementer may want to modify. Never
    *           <code>null</code> or empty.
    * @param request request context object, never <code>null</code>.
    * @return A list of modified rows. Must not be <code>null</code> may be
    *         empty.
    * @throws PSExtensionProcessingException implementation can throw this
    *            exception if any error occurs during processing.
    */
   List processRows(Object[] params, List rows, IPSRequestContext request)
         throws PSExtensionProcessingException;
}