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
package com.percussion.i18n.tmxdom;

import com.percussion.i18n.PSI18nUtils;

import org.w3c.dom.Element;

/**
 * This class wraps the functionality of note DOM element of the TMX document
 * as an easy to use TMX node. The TMX counterpart provides methods to manipulate
 * the note, the most important one being to merge two nodes applying the merge
 * configuration.
 */
public class PSTmxNote
   extends PSTmxLeafNode
   implements IPSTmxNote
{
   /**
    * Constructor. Takes the parent TMX document object and the DOM element
    * representing the note. The language this note associated with is
    * constructed from the supplied DOM element.
    * @param tmxdoc parent TMX document, nust not be <code>null</code>.
    * @param note DOM element for the TMX property to be contstructed, must not
    * be <code>null</code>.
    * @throws IllegalArgumentException if tmxdoc or note is <code>null</code>
    */
   PSTmxNote(IPSTmxDocument tmxdoc, Element note)
   {
      if(tmxdoc == null)
         throw new IllegalArgumentException("tmxdoc must not be null");
      if(note == null)
         throw new IllegalArgumentException("note must not be null");

      m_DOMElement = note;
      m_PSTmxDocument = tmxdoc;
      m_Lang = m_DOMElement.getAttribute(IPSTmxDtdConstants.ATTR_XML_LANG);
   }

   /*
    * Implementation of the method defined in the interface
    */
   public String getLang(){
      return m_Lang;
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void setLang(String lang){
      m_DOMElement.setAttribute(IPSTmxDtdConstants.ATTR_XML_LANG, lang);
      m_Lang = lang;
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void merge(IPSTmxNode node)
      throws PSTmxDomException
   {
      if(node == null)
      {
         throw new IllegalArgumentException("node must not be null for merging");
      }
      else if(!(node instanceof IPSTmxNote))
      {
         throw new PSTmxDomException("onlyOneTypeAllowedToMerge", "IPSTmxNote");
      }

      IPSTmxNote noteSrc = (IPSTmxNote)node;
      String configType = IPSTmxMergeConfig.MERGE_NODEID_TU_NOTE;
      if(getParent() instanceof IPSTmxTranslationUnitVariant)
         configType = IPSTmxMergeConfig.MERGE_NODEID_TUV_NOTE;

      PSTmxConfigParams options = getTMXDocument().getMergeConfig()
            .getConfigParams(configType);
      boolean exists = getValue().equals(noteSrc.getValue());
      if(exists)
      {
         if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_MODIFYIFEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            setValue(noteSrc.getValue());
         }
      }
   }

   /**
    * The default language this node is associated with. A note is always
    * associated with a language. Never <code>null</code> or <code>empty</code>.
    */
   protected String m_Lang = PSI18nUtils.DEFAULT_LANG;
}
