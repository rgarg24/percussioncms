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
package com.percussion.share.dao.impl;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSContentEvent;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.server.PSPurgableFileValue;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSRelationshipCataloger;
import com.percussion.share.dao.PSJcrNodeMap;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemSummaryUtils;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import static com.percussion.share.dao.impl.PSLegacyExceptionUtils.convertException;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Manage R/W of the content item through a 
 * {@link PSJcrNodeMap}.
 * Write operations are done using {@link IPSContentWs}.
 * Read operations are done using JCR repo interface.
 * @author adamgent
 *
 */
@PSSiteManageBean("contentItemDao")
@Transactional(noRollbackFor = Exception.class)
public class PSContentItemDao implements IPSContentItemDao
{

    private IPSContentWs contentWs;
    private IPSContentDesignWs contentDesignWs;
    private IPSCmsObjectMgr cmsObjectMgr;
    private IPSDataItemSummaryService itemSummaryService;
    private IPSIdMapper idMapper;
    private IPSFolderHelper folderHelper;
    private IPSRelationshipCataloger relationshipHelper;
    private IPSSystemWs systemWs;
    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSContentEvent psContentEvent;


    @Autowired
    public PSContentItemDao(IPSContentDesignWs contentDesignWs, IPSContentWs contentWs, IPSIdMapper idMapper,
                            @Qualifier("itemSummaryService") IPSDataItemSummaryService itemSummaryService, IPSFolderHelper folderHelper,
                        IPSCmsObjectMgr cmsObjectMgr, @Qualifier("relationshipCataloger") IPSRelationshipCataloger relationshipHelper,
                            IPSSystemWs systemWs)
    {
        super();
        this.contentDesignWs = contentDesignWs;
        this.contentWs = contentWs;
        this.idMapper = idMapper;
        this.itemSummaryService = itemSummaryService;
        this.folderHelper = folderHelper;
        this.cmsObjectMgr = cmsObjectMgr;
        this.relationshipHelper = relationshipHelper;
        this.systemWs = systemWs;
    }
    

    public Collection<Integer> findAllItemIdsByType(String name) throws PSDataServiceException {
        List<IPSNodeDefinition> nodes = PSContentTypeHelper.loadNodeDefs(name);
        if (nodes.isEmpty())
            return new ArrayList<>();
        
        IPSGuid ctypeId = nodes.get(0).getGUID();
        try
        {
            return cmsObjectMgr.findContentIdsByType(ctypeId.getUUID());
        }
        catch (PSORMException e)
        {
            throw new PSDataServiceException("failed to find item IDs by content type name: " + name, e);
        }
    }
    
   public PSContentItem findItemByPath(String name, String folderPath) throws PSDataServiceException {
        notEmpty(name, "name");
        notEmpty(folderPath, "folderPath");
        
        try
        {
            IPSItemSummary summary = folderHelper.findItem(folderHelper.concatPath(folderPath, name));
            return find(summary.getId());
        }
        catch (Exception e)
        {
            throw new PSDataServiceException("find item by path failed", convertException(e));
        }
    }
    
    
    public IPSItemSummary addItemToPath(IPSItemSummary item, String folderPath) throws PSDataServiceException {
        try
        {
            folderHelper.addItem(folderPath, item.getId());
            return itemSummaryService.find(item.getId());
        }
        catch (Exception e)
        {
            throw new PSDataServiceException("Trying to add item to the folder failed", convertException(e));
        }
    }
    
    public void removeItemFromPath(IPSItemSummary item, String folderPath) throws PSDataServiceException {
        notNull(item, "item");
        notNull(folderPath, "folderPath");
        notEmpty(folderPath, "folderPath");
        
        try
        {
            folderHelper.removeItem(folderPath, item.getId(), false);
        }
        catch (Exception e)
        {
            throw new PSDataServiceException("Trying to remove item from the folder failed", convertException(e));
        }
    }
    
    public PSContentItem findItemByPath(String fullPath) throws PSDataServiceException {
        notNull(fullPath, "fullPath");
        try
        {
            IPSItemSummary summary = folderHelper.findItem(fullPath);
            if (summary == null)
                return null;
            
            return find(summary.getId());
        }
        catch (Exception e)
        {
            throw new PSDataServiceException(convertException(e));
        }
    }

    public void validateDelete(String id, Errors errors) {
        IPSGuid guid = idMapper.getGuid(id);
        PSComponentSummary compSumry = cmsObjectMgr.loadComponentSummary(guid.getUUID());
        String userName = "";
        if (compSumry != null)
        {
            PSLocator locator = compSumry.getEditLocator();
            if (locator.getRevision() != -1)
            {
                // Only current user can delete items s/he has checked out
                if (!PSWebserviceUtils.isItemCheckedOutToUser(compSumry))
                {
                    // Find who has it and report back in the exception
                    userName = compSumry.getCheckoutUserName();
                    errors.reject("object.cannotDeleteInUse", new Object[0], 
                            "User: " + userName + " is editing the item. Failed to delete item." );
                }
            }
        }
    }
    
    public void revisionControlOn(String id) throws LoadException {
        notEmpty(id);
        IPSGuid guid = idMapper.getGuid(id);
        try
        {
            revisionControlOn(guid);
        }
        catch (PSORMException e)
        {
            throw new LoadException("Failed to turn revision control on for id: " + id, e);
        }
    }
    
    @Transactional
    public void revisionControlOn(IPSGuid guid) throws PSORMException {
        /*
         * If its revisionable then we see if we need to update
         * the component summary. If its not revisionable we
         * do not update (you can't turn of revisioning right now).
         */
        PSLocator locator = idMapper.getLocator(guid);
        Integer contentId = locator.getId();
        PSComponentSummary sum = cmsObjectMgr.loadComponentSummary(contentId);
        if ( ! sum.isRevisionLock() ) {
            log.debug("Turning revision lock on for item: {}" , contentId);
            sum.setRevisionLock(true);
            cmsObjectMgr.saveComponentSummaries(singletonList(sum));
        }
    }
    
    public void delete(String id) throws com.percussion.share.dao.IPSGenericDao.DeleteException
    {
        notNull(id, "id");
        IPSGuid guid = idMapper.getGuid(id);
        String uid=guid.toString();
        String path="";
        String substring="";
        try
        {
            try{
                path= folderHelper.findPaths(uid,PSRelationshipConfig.TYPE_RECYCLED_CONTENT).get(0);

            }
            catch (Exception e){
               //Just catching exception in case path is not working
            }
            contentWs.deleteItems(asList(guid));



             substring = uid.substring(uid.lastIndexOf("-") + 1, id.length());
            psContentEvent=new PSContentEvent(id, substring,path, PSContentEvent.ContentEventActions.delete, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
            psAuditLogService.logContentEvent(psContentEvent);
        }
        catch (Exception e)
        {
            psContentEvent=new PSContentEvent(id,substring,path, PSContentEvent.ContentEventActions.delete, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.FAILURE);
            psAuditLogService.logContentEvent(psContentEvent);
            throw new DeleteException(convertException(e));
        }
    }

    public PSContentItem find(String id) throws com.percussion.share.dao.IPSGenericDao.LoadException{
        try {
            return find(id, false);
        } catch (PSDataServiceException e) {
            throw new LoadException(e);
        }
    }
        
    public PSContentItem find(String id, boolean isSummary) throws PSDataServiceException {
        notNull(id, "id");
        IPSItemSummary itemSummary = itemSummaryService.find(id);
        if (itemSummary == null) return null;
        IPSGuid guid = idMapper.getGuid(id);
        List<Node> nodes;
        try
        {
            nodes = contentDesignWs.findNodesByIds(asList(guid), isSummary);
        }
        catch (Exception e)
        {
            throw new LoadException(convertException(e));
        }
        
        if (nodes.isEmpty())
        {
            return null;
        }
        
        Node node = nodes.get(0);
        PSJcrNodeMap nodeMap = new PSJcrNodeMap(node, true);
        PSContentItem item = new PSContentItem();
        PSItemSummaryUtils.copyProperties(itemSummary, item);
        item.setFields(nodeMap);
        if (log.isTraceEnabled()) {
            log.trace("Found item for id: " + id + " item: " + item);
        }
        return item;
    }

    public List<PSContentItem> findAll() throws com.percussion.share.dao.IPSGenericDao.LoadException
    {

        throw new UnsupportedOperationException("findAll is not yet supported");
    }

    public PSContentItem save(PSContentItem contentItem) throws com.percussion.share.dao.IPSGenericDao.SaveException, DeleteException {

        log.debug("Saving object: {}" , contentItem);
        
        boolean isNew = false;
        String id = null;
        try
        {
            notNull(contentItem, "contentItem");
            PSCoreItem coreItem;
            if (contentItem.getId() == null) {
                isTrue(isNotBlank(contentItem.getType()), "Content type missing from: ", contentItem );
                coreItem = contentWs.createItems(contentItem.getType(), 1).get(0);
                isNew = true;
            }
            else {
                IPSGuid guid = idMapper.getGuid(contentItem.getId());
                guid = contentDesignWs.getItemGuid(guid);
                List<PSCoreItem> items = contentWs.loadItems(asList(guid), 
                        true, false, false, true);
                notEmpty(items);
                coreItem = items.get(0);
            }

            for (Entry<String, Object> nvp : contentItem.getFields().entrySet()) {
                PSItemField f = coreItem.getFieldByName(nvp.getKey());
                Object value = nvp.getValue();
                if (f != null) {
                    //CMS-7974 : For file type asset. The value if null was giving attachment not found validation error.
                    if (value == null) {
                        if(f !=null && f.getItemFieldMeta()!=null && f.getItemFieldMeta().isBinary()){
                            value = f.getValue();
                        }else{
                            f.clearValues();
                        }
                    } else {
                        IPSFieldValue fv;
                        f.clearValues();
                        if (value instanceof PSPurgableTempFile) {
                            fv = new PSPurgableFileValue((PSPurgableTempFile) value);
                            f.addValue(fv);
                        } else if (value instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> values = (List<String>) value;
                            for (String val : values) {
                                fv = f.createFieldValue(val);
                                f.addValue(fv);
                            }
                        } else if (value instanceof Long) {
                            fv = f.createFieldValue(Long.toString((Long) value));
                            f.addValue(fv);
                        } else if (value instanceof Integer) {
                            fv = f.createFieldValue(Integer.toString((Integer) value));
                            f.addValue(fv);
                        } else {
                            if (value != null) {
                                fv = f.createFieldValue((String) value);
                                f.addValue(fv);
                            } else {
                                f.clearValues();
                            }
                        }
                    }
                }
            }
            
            // get the folder id to enable asset renaming if required
            IPSGuid folderId = null;
            List<String> paths = contentItem.getFolderPaths();
            if (paths != null && !paths.isEmpty())
            {
                folderId = contentWs.getIdByPath(paths.get(0));
            }
                        
            IPSGuid guid = contentWs.saveItems(singletonList(coreItem), false, false, folderId).get(0);
            id = idMapper.getString(guid);
            
            /*
             * Turn on revisioning if needed.
             */
            boolean revisionable = contentItem.isRevisionable();
            if (revisionable) {
                revisionControlOn(guid);
            }
            
            // add the item to the necessary folders
            if (paths != null)
            {                       
                for(String p : paths)
                {
                    folderHelper.addItem(p, id);
                }
            }
            return find(id);
        }
        catch (Exception e)
        {
            if (e instanceof LoadException)
            {
                if (isNew && id != null)
                {
                    // find may have failed due to insufficient memory, delete the newly created asset
                    delete(id);
                }
            }
            
            throw new SaveException("Error saving object: " + contentItem, convertException(e));
        }

        
    }
    
    public List<String> findOwners(String id, String name, String contentType, String slot)
    {
        return relationshipHelper.findOwners(id, name, contentType, slot);
    }


    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);
}
