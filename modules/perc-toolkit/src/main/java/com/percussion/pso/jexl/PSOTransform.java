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

package com.percussion.pso.jexl;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.percussion.data.PSCachedStylesheet;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.xmldom.PSStylesheetCacheManager;

/**
 * Transforms an XML string with XSLT. 
 * 
 * @author DavidBenua
 *
 */
public class PSOTransform extends PSJexlUtilBase implements IPSJexlExpression
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSOTransform.class);
   
   /**
    * 
    */
   public PSOTransform()
   {
      super();
   }

   /**
    * Transforms an XML string with XSLT. Convenience method without parameters 
    * @param source the source string.  Must be well formed. 
    * @param stylesheetName the name of the stylesheet, relative to the Percussion CMS root. 
    * @return the result of the transform. 
    */
   @IPSJexlMethod(description="Transform a value with an XSLT transform", 
         params={
        @IPSJexlParam(name="source", type="String", description="the source to transform"),
        @IPSJexlParam(name="stylesheetName", type="String", description="the URI of the stylesheet to apply")})
   public String transform(String source, String stylesheetName)
   {
      Map<String,Object> params = new HashMap<String,Object>();
      return transform(source, stylesheetName, params);
   }
   
   /**
    * Transforms an XML string with XSLT.  The source will be wrapped in a 
    * <code>&lt;div class="rxbodyfield"&gt;</code>. The source may contain 
    * multiple elements, but otherwise must be well formed.
    * <p>
    * The parameters may be referenced with <code>&lt;xsl:param...</code> 
    * as a direct child of the <code>&lt;xsl:stylesheet></code> node.   
    * @param source the source string.  
    * @param stylesheetName the name of the stylesheet, relative to the Percussion CMS root. This should be
    * a file url. For example: <code>file:rx_resource/stylesheets/myStylesheet.xsl</code>
    * @param params a map of parameter names and values. 
    * @return the transformed source
    */
   @IPSJexlMethod(description="Transform a value with an XSLT transform", 
         params={
        @IPSJexlParam(name="source", type="String", description="the source to transform"),
        @IPSJexlParam(name="stylesheetName", type="String", description="the file: URL of the stylesheet to apply"),
        @IPSJexlParam(name="params", description="XSLT parameters for stylesheet")})
   public String transform(String source, String stylesheetName, Map<String,Object> params)
   {
      URL styleFile;
      PSCachedStylesheet styleCached = null; 
      
      try
      {
         styleFile = new URL(stylesheetName);
         styleCached = PSStylesheetCacheManager.getStyleSheetFromCache(styleFile);
         
         Transformer nt = styleCached.getStylesheetTemplate().newTransformer();
         
         for(String pkey : params.keySet())
         { 
            Object pval = params.get(pkey);
            log.debug("Adding parameter {} value {}", pkey,  pval);
            nt.setParameter(pkey, pval); 
         }
         Source src = new StreamSource(new StringReader(wrapField(source)));
         StringWriter outString = new StringWriter();
         StreamResult res = new StreamResult(outString);

         nt.transform(src, res);
         
         return outString.toString();

      } catch (Throwable ex)
      {         
         log.error("XSLT Error: {} Error: {}", ex.getMessage());
         log.debug(ex.getMessage(), ex);
      } 
      return "";
   }
   
   /**
    * Wraps a field in <code>&lt;div class="rxbodyfield"&gt;</code>
    * @param field the field to wrap
    * @return the wrapped string. Never null or empty. 
    */
   private static String wrapField(String field)
   {
      StringBuilder sb = new StringBuilder(); 
      sb.append("<div class=\"rxbodyfield\">");
      sb.append(field);
      sb.append("</div>");
      return sb.toString(); 
   }
}
