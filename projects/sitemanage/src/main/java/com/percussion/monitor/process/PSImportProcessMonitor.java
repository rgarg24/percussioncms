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
package com.percussion.monitor.process;

import com.percussion.monitor.service.IPSMonitor;
import com.percussion.monitor.service.PSMonitorService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the state of the import process monitor
 * 
 * @author JaySeletz
 *
 */
public class PSImportProcessMonitor
{
    private static final String CATALOG_PAGES_MSG = " cataloged pages waiting for import";
    private static final String CATALOG_PAGE_MSG = "1 cataloged page waiting for import";
    private static final String NO_CATALOG_PAGE_MSG = "No cataloged pages waiting for import";
    
    private static IPSMonitor monitor = null;
    private static int curCount = 0;
    private static AtomicBoolean indexPause = new AtomicBoolean(false);
    public PSImportProcessMonitor()
    {
        monitor = PSMonitorService.registerMonitor("Import", "Import");
        setCatalogCount(0);
    }
    
    public static void setCatalogCount(int count)
    {
        if (monitor == null) {
            return;
        }
        
        curCount = count;
        String msg = NO_CATALOG_PAGE_MSG;
        if (count > 0) {
            msg = count == 1 ? CATALOG_PAGE_MSG : count + CATALOG_PAGES_MSG;
        }
        
        monitor.setMessage(msg);
    }
    
    public static int getCatalogCount()
    {
        return curCount;
    }
    
}
