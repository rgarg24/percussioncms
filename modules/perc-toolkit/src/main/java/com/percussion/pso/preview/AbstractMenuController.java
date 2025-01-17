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
package com.percussion.pso.preview;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Base class for Rhythmyx menu actions. 
 * Configurable properties, in addition to those of the super class, are: 
 * <ul>
 * <li>siteFolderFinder - the site folder finder bean reference.   
 * <li>testCommunityVisibility - flag that indicates whether community visibility should be used to filter 
 * lists of sites and templates. Defaults to <code>true</code>  
 * </ul>
 *
 * @author DavidBenua
 *
 */
public abstract class AbstractMenuController extends ParameterizableViewController implements Controller
{
   private static final Logger log = LogManager.getLogger(AbstractMenuController.class);
   protected SiteFolderFinder siteFolderFinder = null;
   protected static IPSAssemblyService asm = null;
   protected static IPSSecurityWs secws = null;
   
   protected static PSOObjectFinder objectFinder = null; 
   private boolean testCommunityVisibility = true; 

   /**
    * @see ParameterizableViewController#handleRequestInternal(HttpServletRequest,
    *      HttpServletResponse)
    */
   protected ModelAndView handleRequestInternal(HttpServletRequest request,
         HttpServletResponse response) throws Exception
   {
      return super.handleRequestInternal(request, response);
   }



   /**
    * Find the visible templates for a given item
    * 
    * @param contentid
    * @param sites
    * @return the list of templates. Never <code>null</code> but may be
    *         <code>empty</code>
    * @throws PSException
    * @throws PSAssemblyException
    */
   protected List<IPSAssemblyTemplate> findVisibleTemplates(String contentid, Set<IPSSite> sites)
         throws PSException, PSAssemblyException
   {
      initServices();
      if(sites.size() == 0)
      { 
         log.warn("No matching sites found, no templates will be processed");
         return new ArrayList<>();
      }
      
      PSComponentSummary summary = objectFinder.getComponentSummaryById(contentid);
      List<IPSAssemblyTemplate> alltemps = asm.findTemplatesByContentType(summary.getContentTypeGUID());
      
      return filterVisibleTemplates(alltemps, sites); 
   }

   /**
    * Filters the list of templates to just the visible ones for a give set of sites. 
    * @param alltemps templates to filter
    * @param sites the sites to check 
    * @return the visible templates associated with those sites. 
    */
   protected List<IPSAssemblyTemplate> filterVisibleTemplates(Collection<IPSAssemblyTemplate> alltemps, Set<IPSSite> sites)
   {
      initServices();
      
      List<IPSAssemblyTemplate> visibleTemplates = new ArrayList<>();
      
      if(!this.isTestCommunityVisibility())
      {
         for(IPSAssemblyTemplate t : alltemps)
         {
            if(isTemplateOnSite(t, sites))
            {
               visibleTemplates.add(t); 
            }
         }
         return visibleTemplates; 
      }
      List<IPSGuid> glist = new ArrayList<IPSGuid>(); 
      for(IPSAssemblyTemplate t1 : alltemps)
      {
         glist.add(t1.getGUID()); 
      }
      if(glist.isEmpty())
      { //no templates, return an empty list
          return visibleTemplates; 
      } 
      List<IPSGuid>visGuids = secws.filterByRuntimeVisibility(glist);
      for(IPSAssemblyTemplate t2 : alltemps)
      {
         if(visGuids.contains(t2.getGUID()))
         {
            log.debug("Template {} is visible", t2.getName());
            if(isTemplateOnSite(t2, sites))
            {
               visibleTemplates.add(t2);
            }
         }
         else
         {
            log.debug("Template " + t2.getName() + " is not visible"); 
         }
      }
      return visibleTemplates; 
   }

   /**
    * Find the unique set of sites for a list of locations. 
    * @param locations the location.  Must not be <code>null</code> but may be <code>empty</code>
    * @return the sites.  Never <code>null</code> but may be <code>empty</code>
    */
   protected Set<IPSSite> findSitesFromLocations(List<SiteFolderLocation> locations)
   {
      Set<IPSSite> sites = new HashSet<IPSSite>(); 
      for(SiteFolderLocation loc : locations)
      {
         sites.add(loc.getSite()); 
      }
      return sites; 
   }

   /**
    * Determines if a template is registered for use one or more of the specified sites. 
    * @param t the template. 
    * @param sites the Set of sites. 
    * @return <code>true</code> if the template can be used on one or more of the sites. 
    * <code>false</code> otherwise. 
    */
   protected boolean isTemplateOnSite(IPSAssemblyTemplate t, Set<IPSSite> sites)
   {
      if(sites.size() == 0)
         return true; 
      //check that template is visible on at least one site. 
      for(IPSSite site : sites)
      {
         Set<IPSAssemblyTemplate> temps = site.getAssociatedTemplates();
         
         if(temps.contains(t))
         {
            log.debug("Template " + t.getName() + " is allowed on site " + site.getName());
            
            return true;
         }
      }
      return false; 

   }
   /**
    * Gets the site folder finder. 
    * @return the siteFolderFinder
    */
   public SiteFolderFinder getSiteFolderFinder()
   {
      return siteFolderFinder;
   }

   /**
    * Initialize the service pointers.  
    */
   protected static void initServices()
   {
      if(asm == null)
      {
         secws = PSSecurityWsLocator.getSecurityWebservice(); 
         asm = PSAssemblyServiceLocator.getAssemblyService();
         objectFinder = new PSOObjectFinder();
      }
   }
   
   /**
    * @param siteFolderFinder the siteFolderFinder to set
    */
   public void setSiteFolderFinder(SiteFolderFinder siteFolderFinder)
   {
      this.siteFolderFinder = siteFolderFinder;
   }

   /**
    * Sets the assembly service pointer. Used only for unit testing.
    * @param asm the assembly service to set.  
    */
   public static void setAsm(IPSAssemblyService asm)
   {
      AbstractMenuController.asm = asm;
   }

   /**
    *  Sets the security service pointer. Used only for unit testing.
    * @param secws the secws to set.
    */
   public static void setSecws(IPSSecurityWs secws)
   {
      AbstractMenuController.secws = secws;
   }



   /**
    * Is the community visiblity flag set.
    * @return the testCommunityVisibility
    */
   public boolean isTestCommunityVisibility()
   {
      return testCommunityVisibility;
   }

   /**
    * Sets the community visibility flag.
    * @param testCommunityVisibility the testCommunityVisibility to set
    */
   public void setTestCommunityVisibility(boolean testCommunityVisibility)
   {
      this.testCommunityVisibility = testCommunityVisibility;
   }



   /**
    * Sets the object finder.  Used only for unit testing. 
    * @param objectFinder the objectFinder to set
    */
   public static void setObjectFinder(PSOObjectFinder objectFinder)
   {
      AbstractMenuController.objectFinder = objectFinder;
   }

 
}
