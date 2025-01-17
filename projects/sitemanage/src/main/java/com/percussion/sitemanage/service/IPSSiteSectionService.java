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

package com.percussion.sitemanage.service;

import java.util.List;
import java.util.Map;

import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSCreateExternalLinkSection;
import com.percussion.sitemanage.data.PSCreateSectionFromFolderRequest;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSMoveSiteSection;
import com.percussion.sitemanage.data.PSReplaceLandingPage;
import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.sitemanage.data.PSSiteBlogPosts;
import com.percussion.sitemanage.data.PSSiteBlogProperties;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSSiteSectionProperties;
import com.percussion.sitemanage.data.PSUpdateSectionLink;

/**
 * The CRUD operation for site sections.
 *
 * @author YuBingChen
 */
public interface IPSSiteSectionService extends IPSDataService<PSSiteSection, PSSiteSection, String>
{
    /**
     * Creates a section according to the specified request info.
     * 
     * @param req the info for creating a section, never <code>null</code>.
     * 
     * @return the created site section, never <code>null</code>.
     */
    public PSSiteSection create(PSCreateSiteSection req) throws PSDataServiceException;

    /**
     * Creates an external link section according to the specified request info.
     * 
     * @param req the info for creating a section, never <code>null</code>.
     * 
     * @return the created site section, never <code>null</code>.
     */
    public PSSiteSection createExternalLinkSection(PSCreateExternalLinkSection req) throws PSValidationException, IPSPathService.PSPathNotFoundServiceException, PSSiteSectionException;

    /**
     * Creates a section link with between the supplied parent and target. Adds the targetSectionGuid to the submenu 
     * slot of the parentSectionGuid.
     * 
     * @param targetSectionGuid, the guid of the target section, must not be <code>null</code>.
     * @param parentSectionGuid, the guid of the parent section, must not be <code>null</code>.
     * @return the created site section, never <code>null</code>.
     */
    public PSSiteSection createSectionLink(String targetSectionGuid, String parentSectionGuid) throws PSSiteSectionException;
    

    /**
     * Create a section from an existing folder and landing page
     * 
     * @param req The request specifying the folder and landing page, not <code>null</code>, both values must be valid.
     * 
     * @return The created section, not <code>null</code>.
     */
    PSSiteSection createSectionFromFolder(PSCreateSectionFromFolderRequest req) throws PSDataServiceException;


    /**
     * Removes the section link between the supplied  targetSectionGuid and parentSectionGuid. 
     * 
     * @param sectionGuid, the guid of the target section, must not be <code>null</code>.
     * @param parentSectionGuid, the guid of the parent section, must not be <code>null</code>.
     * @return returns a dummy PSNoContent object, never <code>null</code>.
     */
    public PSNoContent deleteSectionLink(String sectionGuid, String parentSectionGuid);

    /**
     * Updates a section according to the specified request info.
     * 
     * @param req the info for updating a section, never <code>null</code>.
     * 
     * @return the updated section object, never <code>null</code>.
     */
    public PSSiteSection update(PSSiteSectionProperties req) throws PSDataServiceException;
    
    /**
     * Updates a section according to the specified request info.
     * 
     * @param req the info for updating a section, never <code>null</code>.
     * 
     * @return the updated section object, never <code>null</code>.
     */
    public PSSiteSection updateSectionLink(PSUpdateSectionLink req) throws PSSiteSectionException;
    
    /**
     * Updates a section according to the specified request info.
     * 
     * @param req the info for updating a section, never <code>null</code>.
     * 
     * @return the updated section object, never <code>null</code>.
     */
    public PSSiteSection updateExternalLink(String sectionGuid, PSCreateExternalLinkSection req) throws PSSiteSectionException, PSValidationException;
    
    /**
     * Replaces the landing page for the specified section.
     * 
     * @param request the request info contains the info of the new landing page
     * and the target section, never <code>null</code>.
     * 
     * @return the result of the operation, never <code>null</code>.
     */
    public PSReplaceLandingPage replaceLandingPage(PSReplaceLandingPage request) throws PSDataServiceException;
    
    /**
     * Moves a section to different location. The new target location may or
     * may not be under the same parent navigation node.
     * 
     * @param req the request info, never <code>null</code>.
     * 
     * @return the target section, which contains re-arranged child nodes, 
     * never <code>null</code>.
     */
    public PSSiteSection move(PSMoveSiteSection req) throws PSValidationException, PSSiteSectionException;
    
    /**
     * Deletes the specified section and all its descendant (child, grand-child,
     * ...etc) sections.
     * <p>
     * Note, delete a section only delete navigation nodes, but do not delete
     * related folders, landing pages, or any items (pages or resources) within
     * the related folders and sub folders.
     * 
     * @param id the ID of the section, never <code>null</code> or empty.
     */
     public void delete(String id) throws PSValidationException, DataServiceSaveException;

     /**
      * Converts the specified section and all its descendant (child, grand-child,
      * ...etc) sections.
      * <p>
      * 
      * @param id the ID of the section, never <code>null</code> or empty.
      */
      public void convertToFolder(String id) throws PSValidationException;

      /**
      * Loads the root of the navigation for the specified site.
      * 
      * @param siteName the name of the site, not blank.
      * 
      * @return the root of the navigation, never <code>null</code>.
      */
     public PSSiteSection loadRoot(String siteName) throws PSSiteSectionException, PSNotFoundException;

     /**
      * Loads the entire tree nodes for the specified site.
      * 
      * @param siteName the name of the specified site, not blank.
      * 
      * @return the tree nodes of the site, never <code>null</code>.
      */
     public PSSectionNode loadTree(String siteName) throws PSSiteSectionException, PSNotFoundException;

     /**
      * Loads all child sections of the specified site section.
      *  
      * @param section the specified site section, not <code>null</code>.
      * 
      * @return a list of child section in the same order as they are defined
      * in the specified section, never <code>null</code>, may be empty.
      */
     public List<PSSiteSection> loadChildSections(PSSiteSection section);
     
     /**
      * Gets the list of blogs for the specified site, if siteName is null/empty then returns the blogs for all sites
      * @param siteName never <code>null</code>, not empty
      * @return returns the list of PSSiteBlogProperties objects.
      */
     public List<PSSiteBlogProperties> getBlogsForSite(String siteName) throws PSValidationException;
     
     /**
      * Gets blogs for all sites on the system
      * @return a list of PSSiteBlogProperties objects for all sites on the system
      */
     public List<PSSiteBlogProperties> getAllBlogs() throws PSDataServiceException;
     
     
     /**
      * Finds all the templates used by blogs, if siteName is supplied, limits the templates used by the blogs from that
      * site only.
      * @param siteName if blank finds all the templates used by all blogs in the system.
      * @return The list of the template ids, never <code>null</code> may be empty.
      * @throws PSSiteSectionException if the supplied siteName is not blank and failed to load that site.
      */
     public List<String> findAllTemplatesUsedByBlogs(String siteName) throws PSDataServiceException, PSNotFoundException;

     /**
      * Gets posts for the specified blog.
      * 
      * @param id of the blog, never blank.
      * @return blog posts, ordered alphabetically by title (post link text).
      */
     public PSSiteBlogPosts getBlogPosts(String id) throws PSValidationException, PSSiteSectionException;
     
     /**
      * Gets the blog post template id for the specified blog.
      * 
      * @param path of the blog, never blank.
      * @return id of the blog post template, <code>null</code> if the path does not represent a blog.
      */
     public String getBlogPostTemplateId(String path);

    /**
     * When a site is being published, first the security files according to its
     * configuration must be generated, in order to reflect the site sections
     * configuration. The created file is: <code>security-url-pattern.xml</code>
     * .
     * 
     * @param site the site (assumed not <code>null</code>) for which we want to
     *            create the configuration files.
     */
   public void generateSecurityConfigurationFiles(IPSSite site) throws DataServiceSaveException, PSSiteSectionException, PSNotFoundException;

    /**
     * @param sitename
     */
    public void clearSectionsSecurityInfo(String sitename) throws PSSiteSectionException, PSNotFoundException;
    
    /**
     * @param siteName
     * @param tempMap a map of to and from template ids to modify.
     */
    public void updateSectionBlogTemplates(String siteName, Map<String, String> tempMap) throws PSSiteSectionException, PSNotFoundException;

    /**
     * (Runtime) Exception is thrown when an unexpected error occurs in this
     * service.
     */
    public static class PSSiteSectionException extends RuntimeException
    {
       /**
        * Generated serial number.
        */
       private static final long serialVersionUID = 1L;

       /**
        * Default constructor.
        */
       public PSSiteSectionException()
       {
          super();
       }

       /**
        * Constructs an exception with the specified detail message and the
        * cause.
        * 
        * @param message the specified detail message.
        * @param cause the cause of the exception.
        */
       public PSSiteSectionException(String message, Throwable cause)
       {
          super(message, cause);
       }

       /**
        * Constructs an exception with the specified detail message.
        * 
        * @param message the specified detail message.
        */
       public PSSiteSectionException(String message)
       {
          super(message);
       }

       /**
        * Constructs an exception with the specified cause.
        * 
        * @param cause the cause of the exception.
        */
       public PSSiteSectionException(Throwable cause)
       {
          super(cause);
       }
    }

}
