package com.percussion.soln.segment.rx.effect;

import static java.util.Arrays.*;

import java.util.List;

import javax.jcr.RepositoryException;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentTypeMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;

public class RxContentTypeHelper implements IContentTypeHelper {
    
    private IPSGuidManager guidManager;
    private IPSContentTypeMgr contentTypeManager;
    private IPSCmsObjectMgr cmsObjectManager;
    
    public RxContentTypeHelper(IPSGuidManager guidManager,
            IPSContentTypeMgr contentTypeManager,
            IPSCmsObjectMgr cmsObjectManager) {
        super();
        this.guidManager = guidManager;
        this.contentTypeManager = contentTypeManager;
        this.cmsObjectManager = cmsObjectManager;
    }
    
    public RxContentTypeHelper() {
        if (cmsObjectManager == null)
            cmsObjectManager = PSCmsObjectMgrLocator.getObjectManager();
        if (guidManager == null)
            guidManager = PSGuidManagerLocator.getGuidMgr();
        if (contentTypeManager == null)
            contentTypeManager = PSContentMgrLocator.getContentMgr();
    }

    public String retrieveContentTypeNameForItem(int contentId) throws RepositoryException {
        PSComponentSummary summary = cmsObjectManager.loadComponentSummary(contentId);
        long contentTypeId = summary.getContentTypeId();
        IPSGuid contentTypeGuid = guidManager.makeGuid(contentTypeId, PSTypeEnum.NODEDEF);
        List<IPSNodeDefinition> defs = contentTypeManager.loadNodeDefinitions(asList(contentTypeGuid));
        if (defs == null || defs.isEmpty() || defs.size() != 1)
            throw new IllegalStateException("The content item did not have a content type: " + defs);
        IPSNodeDefinition def = defs.get(0);
        return def.getInternalName();
        
    }

}