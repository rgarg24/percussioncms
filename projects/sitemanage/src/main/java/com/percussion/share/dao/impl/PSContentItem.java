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
package com.percussion.share.dao.impl;

import com.percussion.share.data.IPSContentItem;
import com.percussion.share.data.PSDataItemSummary;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.Validate.notNull;

/**
 * A Generic Low level representation of an item in the system backed
 * by a Rhythmyx content item.
 * @author adamgent
 *
 */
public class PSContentItem extends PSDataItemSummary implements IPSContentItem
{

    /**
     * never <code>null</code>.
     */
    private Map<String, Object> fields = new HashMap<>();
    

    /**
     * @{inheritDoc}
     */
    public Map<String, Object> getFields()
    {
        return fields;
    }

    /**
     * @{inheritDoc}
     */
    public void setFields(Map<String, Object> fields)
    {
        notNull(fields, "fields");
        this.fields = fields;
    }
    
    

    /**
     * Well not really safe to serialize
     */
    private static final long serialVersionUID = -3451673795623212592L;

}
