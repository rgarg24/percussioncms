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

package com.percussion.pso.transform;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.effects.PSOSetFieldOnSlottedItemEffect;
import com.percussion.pso.utils.PSOSlotContents;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.File;

/***
 * Extension for setting the field on an item when it is saved and has an item in the specified slot. 
 * 
 * @see com.percussion.extension.IPSItemInputTransformer
 * @see com.percussion.extension.IPSRequestPreProcessor
 * @author natechadwick
 *
 */
public class PSOSetFieldOnSlottedItemTransform  implements IPSItemInputTransformer, IPSRequestPreProcessor,com.percussion.extension.IPSResultDocumentProcessor {

	private static final Logger log = LogManager.getLogger(PSOSetFieldOnSlottedItemTransform.class);
	private IPSGuidManager mGmgr;
	private IPSContentWs mCws;

	
	/***
	 * Inner class for handling the user configured parameters for the extension
	 * on the Relationship Effect parameters dialog.
	 * @author natechadwick
	 *
	 */
	private class ConfiguredParams{

		protected String fieldName;
		protected String valueIfEmpty;
		protected String valueIfNotEmpty;		
		protected String slotName;
		
		/***
		 * Constructor to initialize a new parameter object
		 * @param params
		 */
		protected ConfiguredParams(Object[] params){
			
			if(params!=null){
				
				//Make sure that all the parameters have been supplied.
				if(params.length<4)
				{
					throw new IllegalArgumentException("All parameters are required!");
				}
						
				if(params[0]!=null){
					fieldName=params[0].toString();
					log.debug("fieldName={}", params[0]);
				}else{
					fieldName=null;
				}
				
				if(params[1]!=null){
					valueIfEmpty=params[1].toString();
					log.debug("valueIfEmpty={}", params[1]);
				}else{
					valueIfEmpty=null;
				}
				
				if(params[2]!=null){
					valueIfNotEmpty=params[2].toString();
					log.debug("valueIfNotEmpty={}", params[2]);
				}else{
					valueIfNotEmpty=null;
				}
				
				if(params[3]!=null){				
					slotName = params[3].toString().trim();
					log.debug("slotName=" + params[3]);
				}else{
					slotName=null;
				}
	
			}
			
			if(params == null || fieldName== null || slotName==null)
			{
				throw new IllegalArgumentException("Field Name, and SlotName parameters must be set.");
			}
		}
	}

	public void preProcessRequest(Object[] params, IPSRequestContext request)
			throws PSAuthorizationException, PSRequestValidationException,
			PSParameterMismatchException, PSExtensionProcessingException {
		
		IPSGuidManager gmgr = getGuidManager();
		IPSContentWs cws = getContentWebService();
		ConfiguredParams configParams = null;
		
		try{
			configParams = new ConfiguredParams(params);
		}catch(IllegalArgumentException e){
			log.warn(e.getLocalizedMessage());
			return;
		}
		
		String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
		if(contentid==null){
			return;
		}
	
		//Get the current edit revision
		String revision = request.getParameter(IPSHtmlParameters.SYS_REVISION);
		if(revision == null){
			revision = "-1"; //If no revision is on the request go revisionless
		}
		
		//See if we are being triggered via the effect context (like on a Purge of a
		//slot item), if we are then skip processing. 
		String processedFlag = request.getParameter(PSOSetFieldOnSlottedItemEffect.PROCESSED_FLAG);
		
		if(processedFlag != null){
			if(Boolean.parseBoolean(processedFlag) == true){
				log.debug("Skipping transform as update handled by effect.");
				return;
			}
		}else{
			log.debug("No processed flag detected.");
		}
		
		PSRelationshipFilter filter = new PSRelationshipFilter();
		
		IPSGuid itemGuid = gmgr.makeGuid(contentid,PSTypeEnum.LEGACY_CONTENT);   
		PSLocator ownerLoc = gmgr.makeLocator(itemGuid);
		ownerLoc.setRevision(Integer.parseInt(revision));
		filter.setOwner(ownerLoc);
		filter.setName(PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY);
		filter.limitToEditOrCurrentOwnerRevision(true);
		
		IPSTemplateSlot slot = PSOSlotContents.getSlot(configParams.slotName);
		if(slot != null){
			filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slot.getGUID().getUUID()));
		}else{
			return;
		}

		boolean found = false;
		try{
			//Get all AA relationships and if we find one including the specified slot
			//set the specified field to the specified value;
			for (PSAaRelationship rel : cws.loadContentRelations(filter,true)) {
				if(rel.getSlotName().equals(configParams.slotName)){
					request.setParameter(configParams.fieldName, (configParams.valueIfNotEmpty + ""));
					found=true;
					log.debug("Found item in slot, setting {} to {}",configParams.fieldName, (configParams.valueIfNotEmpty ));
					break;
				}
		  }
			if(!found){
				request.setParameter(configParams.fieldName, (configParams.valueIfEmpty + ""));
				log.debug("No item found in slot, setting {} to {}",configParams.fieldName, (configParams.valueIfEmpty));
			}
		} catch (PSErrorException e) {
			log.error("Error processing slot relationships for item, Error: {}", PSExceptionUtils.getMessageForLog(e));
			log.debug(PSExceptionUtils.getDebugMessageForLog(e));
		}finally{} 
	         
	}

	
	private IPSGuidManager getGuidManager(){
		if(mGmgr == null){
			mGmgr = PSGuidManagerLocator.getGuidMgr();	
		}
		return mGmgr;
	}

	
	private IPSContentWs getContentWebService(){
		if(mCws == null){
			mCws = PSContentWsLocator.getContentWebservice();	
		}
		return mCws;
	}

	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		log.info("Extension Initialized.");		
	}

	public boolean canModifyStyleSheet() {
		// TODO Auto-generated method stub
		return false;
	}

	public Document processResultDocument(Object[] arg0,
			IPSRequestContext arg1, Document arg2)
			throws PSParameterMismatchException, PSExtensionProcessingException {
		// TODO Auto-generated method stub
		return null;
	}
		
}
