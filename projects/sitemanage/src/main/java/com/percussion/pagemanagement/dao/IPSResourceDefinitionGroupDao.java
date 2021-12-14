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
package com.percussion.pagemanagement.dao;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService.PSResourceDefinitionNotFoundException;
import com.percussion.pagemanagement.service.PSResourceServiceException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;

import java.util.List;

/**
 * 
 * Low level Retrieving of Resource Definitions.
 * 
 * @author adamgent
 *
 */
public interface IPSResourceDefinitionGroupDao extends IPSGenericDao<PSResourceDefinitionGroup, String>
{


    PSResourceDefinitionGroup find(String id) throws PSDataServiceException;

    List<PSResourceDefinition> findAllResources() throws PSResourceServiceException, PSDataServiceException;
    
    /**
     * Finds a resource based on the uniqueId.
     * 
     * @param uniqueId never <code>null</code>.
     * @return maybe <code>null</code>.
     * @throws PSResourceDefinitionNotFoundException 
     */
    PSResourceDefinition findResource(String uniqueId) throws PSDataServiceException;
    
    /**
     * Finds the primary {@link PSAssetResource} for the given content type.
     * @param contentType never <code>null</code>.
     * @return maybe <code>null</code>.
     * @throws PSResourceDefinitionNotFoundException
     */
    PSAssetResource findAssetResourceForType(String contentType) throws PSDataServiceException;
    
    /**
     * Finds all asset resources definitions for a given content type.
     * @param contentType
     * @return never <code>null</code>, maybe empty.
     */
    List<PSAssetResource> findAssetResourcesForType(String contentType) throws PSResourceServiceException, PSDataServiceException;
    
    /**
     * Finds resources associated to a legacy template.
     * @param template never <code>null</code>.
     * @return never <code>null</code>, maybe empty.
     */
    List<PSAssetResource> findAssetResourcesForLegacyTemplate(String template) throws PSResourceServiceException, PSDataServiceException;;
    
}
