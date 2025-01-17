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
package com.percussion.services.assembly.impl;

import com.percussion.security.SecureStringUtils;
import com.percussion.utils.jsr170.IPSPropertyInterceptor;
import com.percussion.utils.xml.PSSaxCopier;
import com.percussion.utils.xml.PSSaxHelper;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * Cleanup unwanted div tags in the final assembled output. Div tags whose
 * class attribute is equal to 'rxbodyfield' will be removed. This must always be
 * the last property interceptor that is run that requires XML input.
 * <p>
 * This cleanup also removes CDATA sections, which are used to isolate 
 * javascript code that would otherwise mess up XML processing. 
 */
public class PSDivTagCleanup implements IPSPropertyInterceptor
{
  
   public Object translate(Object originalValue)
   {
      if (originalValue instanceof String)
      {
         String oValue = (String)originalValue;
         if (StringUtils.isBlank(oValue) ||
         !(SecureStringUtils.isXML((String)originalValue)
                 ||SecureStringUtils.isHTML((String)originalValue)))
         {
            return originalValue;
         }
         try
         {
            // Since this code may be stripping the "root" element in the body,
            // add a root element before and remove that element after
            oValue = "<div>" + oValue + "</div>";
            String rValue = PSSaxHelper.parseWithXMLWriter(oValue,
                  ContentHandler.class, (Object[]) null);
            rValue = rValue.substring(5);
            rValue = rValue.substring(0, rValue.length() - 6);
            return rValue;
         }
         catch (Exception e)
         {
            PSTrackAssemblyError
               .addProblem("Problem processing div tag cleanup", e);
            throw new RuntimeException(e);
         }
      }
      else
      {
         return originalValue;
      }
   }
   
   /**
    * Copier handler that skips the outermost div tag(s) that have the
    * rxbodyfield attribute. In addition, this overrides the
    * <code>startElement(String, String, String, Attributes)</code>
    * method so that CDATA sections will be processed
    * as regular text.
    * 
    * @author dougrand
    */
   public static class ContentHandler extends PSSaxCopier
   {      

      /**
       * Ctor
       * 
       * @param writer the output writer, assumed never <code>null</code>
       */
      public ContentHandler(XMLStreamWriter writer) 
      {
         super(writer, new HashMap<>(), true);
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.percussion.utils.xml.PSSaxCopier#startElement(java.lang.String,
       *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
       */
      @Override
      @SuppressWarnings("unused")
      public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
      {
         resetCharCount();
         String classAttr = 
            StringUtils.defaultString(attributes.getValue("class"));
         if(qName.toLowerCase().equals("div") && 
            ms_skipClassList.contains(classAttr.toLowerCase()))
         {
            m_stack.push(State.SKIP);
            // Skipping the div, will skip the end element as well
         }
         else
         {
            m_stack.push(State.WRITE);
            super.startElement(uri, localName, qName, attributes);
         }
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.percussion.utils.xml.PSSaxCopier#endElement(java.lang.String,
       *      java.lang.String, java.lang.String)
       */
      @Override
      public void endElement(String uri, String localName, String qName)
            throws SAXException
      {
         if(!m_stack.pop().equals(State.SKIP))
         {
            super.endElement(uri, localName, qName);
         }
      }

      /* (non-Javadoc)
       * @see com.percussion.utils.xml.PSSaxCopier#startCDATA()
       */
      @SuppressWarnings("unused")
      @Override
      public void startCDATA() throws SAXException
      {
         // Noop here will prevent CDATA from being quoted in the output
         // see the superclass to understand the behavior
      }

      /**
       * A stack to keep track of the current tag nesting posistion.
       * Never <code>null</code>, may be empty.
       */
      private Stack<State> m_stack = new Stack<>();
      
      /**
       * A simple enumeration to track the write state for the tags
       */
      private enum State 
      {
         /**
          * The WRITE state will write the start and end tags.
          */
         WRITE,
         /**
          * The SKIP state tells us not to write the start and end tags.
          */
         SKIP
      }

   }
   
   /**
    * List class names that if found on a div tag indicate that
    * the div should be removed.
    */
   static final List<String> ms_skipClassList = 
      new ArrayList<>(2);
   
   static
   {
      ms_skipClassList.add("rxbodyfield");
      ms_skipClassList.add("rx_ephox_inlinevariant");
   }
}
