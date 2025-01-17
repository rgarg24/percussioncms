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
package com.percussion.i18n.rxlt;

import com.percussion.error.PSExceptionUtils;
import com.percussion.i18n.PSTmxResourceBundle;
import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxHeader;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;

/**
 * This class generates a single TMX document out of the TMX documents for all
 * slected sections using appropriate merge configurations. The following logic
 * is used to merge various TMX documents:
 * <ol>
 * <li>Create an empty TMX document</li>
 * <li>Set merge config file {@link #SECTION_MERGE_CONFIG_FILE} to this document</li>
 * <li>Generate TMX documents for every selected section and merge with that in
 * step 1</li>
 * <li>Add all supported languages and correspond stubs to each translational
 * unit</li>
 * <li>Set a new merge configuration to the TMX document depending on user's
 * choice to keep only missing keys or to keep all. This is required as we need
 * to merge the TMX document of the Rhythmyx server to preserve the existing
 * translations.</li>
 * </li>Load the TMX Document from server and merge with the above</li>
 * <li>Save the TMX document (merge result) to the file specified by the user.
 * </ol>
 * <p>
 * This file is what goes to the translation service to translate the missing
 * pieces. The translated copy then finally merged with the server TMX document
 * using the action {@link IPSActionHandler#ACTIONID_MERGE_MASTER}.
 * </p>
 * <p>
 * <em>Note that whenever we generate TMX document from the Rhythmyx Content
 * Manager, we merge the server TMX document. This is important to get all the
 * existing translations preserved.
 * </em>
 * </p>
 */
public class PSGenerateTMXResourcesActionHandler
   implements IPSActionHandler
{
   /*
    * Implementation of the method defined in the interface
    */
   public void process(Element cfgdata)
      throws PSActionProcessingException

   {
      if(cfgdata == null)
      {
         throw new IllegalArgumentException("cfgdata must not be null");
      }

      PSCommandLineProcessor.logMessage("processingAction",
         cfgdata.getAttribute(PSRxltConfigUtils.ATTR_NAME));

      String rxroot = cfgdata.getOwnerDocument()
         .getDocumentElement().getAttribute(PSRxltConfigUtils.ATTR_RXROOT);
      String outputfilepath = cfgdata.getAttribute("outputfile");
      String keepmissingonly = cfgdata.getAttribute("keepmissingkeysonly");

      //build the empty local master tmx document
      PSTmxDocument tmxDocSectionMaster = null;

      try
      {
         /*
          * Create a new TMX document. All TMX document generated for individual
          * sections shall be merged with this.
          */
         tmxDocSectionMaster = new PSTmxDocument();
         //load and set the merge config document
         try(InputStream is = getClass()
                 .getResourceAsStream(SECTION_MERGE_CONFIG_FILE)) {
            tmxDocSectionMaster.setMergeConfigDoc(
                    PSXmlDocumentBuilder.createXmlDocument(is, false));
            //
            NodeList nl = cfgdata.getElementsByTagName("section");
            Element sect = null;
            IPSTmxDocument tmxDoc = null;
            for (int i = 0; nl != null && i < nl.getLength(); i++) {
               sect = (Element) nl.item(i);
               if (sect.getAttribute("process").equalsIgnoreCase("yes")) {
                  try {
                     //Get the TMX document for the current section
                     tmxDoc = processSection(sect);
                     //Merge with section master TMX document
                     tmxDocSectionMaster.merge(tmxDoc);
                  } catch (PSSectionProcessingException e) {
                     PSCommandLineProcessor.logMessage("errorMessageException",
                             e.getMessage());
                  }
               }
            }
         }

         //get the list of languages from the database
         Object[] langs = null;
         langs = PSLocaleHandler.getLocaleStrings(rxroot);
         /*
          * Add these to the TMX document. This actually creates stubs for all
          * languages
          */
         for(int i=0; langs!=null && i<langs.length;i++)
            tmxDocSectionMaster.addLanguage(langs[i].toString());

         Document mergeDoc = null;
         if(keepmissingonly.equalsIgnoreCase("yes"))
         {
            //User chose to generate only the missing keys.
            try(InputStream is = getClass().getResourceAsStream(
                    MASTER_MERGE_CONFIG_FILE_MISSING_ONLY)) {
               mergeDoc = PSXmlDocumentBuilder.createXmlDocument(is
                               , false);
            }
         }
         else
         {
            try(InputStream is = getClass()
                    .getResourceAsStream(MASTER_MERGE_CONFIG_FILE)) {
               mergeDoc = PSXmlDocumentBuilder.createXmlDocument(is, false);
            }
         }

         /*
          * Load the TMX document from the server
          */
         IPSTmxDocument tmxDocServer =
            new PSTmxDocument(PSTmxResourceBundle.getMasterResourceDoc(rxroot));

         /*
          * Set the merge configuration document. We always merge the server
          * TMX doc with local one so that we have all the tranlations from the
          * server TMX document.
          */
         tmxDocSectionMaster.setMergeConfigDoc(mergeDoc);
         tmxDocSectionMaster.merge(tmxDocServer);

         //make sure to set the language tool name and version in the header
         IPSTmxHeader header = tmxDocSectionMaster.getHeader();
         header.setProperty(IPSTmxHeader.PROP_CREATION_TOOL,
            PSRxltMain.PROGRAM_NAME);
         header.setProperty(IPSTmxHeader.PROP_CREATION_TOOL_VERSION,
            PSRxltMain.getVersionNumberString());

         //Save the file
         File file = new File(outputfilepath); //Should it be relative to rxroot??

         //create parent directories if do not exist
         if(file.getParentFile() != null)
            file.getParentFile().mkdirs();

         PSCommandLineProcessor.logMessage("savingGeneratedTMXFile",
            file.getCanonicalPath());
         tmxDocSectionMaster.save(file, false);
      }
      catch(Exception e) //catch any Exception
      {
         PSCommandLineProcessor.logMessage("processFailedError", PSExceptionUtils.getMessageForLog(e));
         throw new PSActionProcessingException(e.getMessage(),e);
      }
      PSCommandLineProcessor.logMessage("processFinished", "");
   }

   /**
    * Helper method to process a given section
    * @param section DOM element storing the config options for the section to
    * be processed. Must not be <code>null</code>.
    * @return TMX document object for this section, never <code>null</code>
    * @throws PSSectionProcessingException if any error occurs during
    * processing of the section.
    */
   private IPSTmxDocument processSection(Element section)
      throws PSSectionProcessingException
   {
      String temp = section.getAttribute("sectionid");
      int sectionid = Integer.parseInt(temp);
      IPSSectionHandler handler = null;
      switch(sectionid)
      {
         case IPSSectionHandler.SECTIONID_CMS_TABLES:
            handler = new PSCmsTablesSectionHandler();
            break;
         case IPSSectionHandler.SECTIONID_XSL_STYLESHEETS:
            handler = new PSXslStylesheetsSectionHandler();
            break;
         case IPSSectionHandler.SECTIONID_CONTENT_EDITORS:
            handler = new PSContentEditorsSectionHandler();
            break;
         case IPSSectionHandler.SECTIONID_EXTENSION_RESOURCES:
            handler = new PSExtensionResourcesSectionHandler();
            break;
         case IPSSectionHandler.SECTIONID_JSPS:
            handler = new PSJspHandler();
            break;
      }
      return handler.process(section);
   }

   /**
    * Name of the merge configuration file used to generate the TMX file by
    * merging all sections.
    */
   static final String SECTION_MERGE_CONFIG_FILE = "configmergesections.xml";

   /**
    * Name of the merge configuration file used to generate the keys when user
    * chooses the option not to generate missingonly keys.
    */
   static final String MASTER_MERGE_CONFIG_FILE = "configmergemaster.xml";

   /**
    * Name of the merge configuration file used to generate the keys that are
    * present in the local TMX document but not in the server TMX resource
    * bundle. This config file is used only if user chooses to generate only
    * missing keys in the UI. This option is useful to generate the keys that
    * are added to Rhythmyx Content manager by adding/modifying  new workflow
    * element, variant etc.
    */
   static final String MASTER_MERGE_CONFIG_FILE_MISSING_ONLY =
      "configmergemastermissingonly.xml";
}
