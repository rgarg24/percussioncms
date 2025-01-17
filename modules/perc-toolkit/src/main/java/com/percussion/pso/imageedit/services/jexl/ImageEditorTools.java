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
package com.percussion.pso.imageedit.services.jexl;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManagerLocator;
import com.percussion.utils.tools.PSCopyStream;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 *
 *
 * @author DavidBenua
 *
 */
public class ImageEditorTools extends PSJexlUtilBase implements IPSJexlExpression
{

   private static final Logger log = LogManager.getLogger(ImageEditorTools.class);
   private ImageSizeDefinitionManager isdm = null; 
   
   private void initServices()
   {
      if(isdm == null)
      {
         isdm = ImageSizeDefinitionManagerLocator.getImageSizeDefinitionManager();
      }
   }
   
   /**
    * Gets an image size definition based on the code.  Exposes 
    * underlying service functionality to JEXL.  
    * @param code the size code
    * @return the size defintion or <code>null</code> if the size 
    * is not defined. 
    */
   @IPSJexlMethod(description="get image size definition by code", 
         params={@IPSJexlParam(name="code",description="size code")})
   public ImageSizeDefinition getImageSizeDefinition(String code)
   {
      initServices();
      return isdm.getImageSize(code); 
   }
   
   /**
    * Gets a child node based on the size code. 
    * The child node name and size code property name are found
    * from the <code>ImageSizeDefinitionManager</code>
    * @param itemNode the item Node. Must not be <code>null</code>.
    * @param sizeCode the desired size code.  Must not be <code>null</code>.
    * @return the image child node of the desired size. May be 
    * <code>null</code> if the child node does not exist in this item. 
    * @throws RepositoryException
    */
   @IPSJexlMethod(description="gets a child node of the appropriate size",
         params={@IPSJexlParam(name="itemNode",type="javax.jcr.Node",description="item node"),
         @IPSJexlParam(name="sizeCode", description="desired size code")})
   public Object getSizedNode(Node itemNode, String sizeCode) throws RepositoryException
   {
      String emsg; 
      initServices();
      if(StringUtils.isBlank(sizeCode))
      {
         emsg = "the size code must not be null or empty"; 
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      String childName = isdm.getSizedImageNodeName();
      String sizeProperty = isdm.getSizedImagePropertyName();
      
      NodeIterator children = itemNode.getNodes(childName);
      while(children.hasNext())
      {
         Node child = children.nextNode();
         Property sizeProp = child.getProperty(sizeProperty);
         if(sizeProp != null)
         {
            String size = sizeProp.getString(); 
            if(StringUtils.isNotBlank(size) && sizeCode.equals(size))
            {
               log.debug("found matching size code {}", size);
               return child; 
            }
         }
      }
      log.debug("size not found: {}", sizeCode);
      return null; 
   }
 
   /**
    * Tests the existence of a child node based on the size code. 
    * The child node name and size code property name are found
    * from the <code>ImageSizeDefinitionManager</code>
    * @param itemNode the item Node. Must not be <code>null</code>.
    * @param sizeCode the desired size code.  Must not be <code>null</code>.
    * @return <code>true> if the image child node of the desired size exists.
    * @throws RepositoryException
    */ 
   @IPSJexlMethod(description="tests the existence of a child node of the appropriate size",
         params={@IPSJexlParam(name="itemNode",description="item node"),
         @IPSJexlParam(name="sizecode", type="java.lang.String", description="desired size code")})
   public boolean hasSizedNode(Node itemNode, String sizeCode) throws RepositoryException
   {
      Node child = (Node) getSizedNode(itemNode, sizeCode);
      if(child != null)
      { 
         return true;
      }
      return false; 
   }
   
   @IPSJexlMethod(description="gets the URL of the failure image defined in the image editor configuration file.",
         params={})
   public String getFailureImageURL()
   {
      initServices(); 
      String path = isdm.getFailureImagePath();
      return "/Rhythmyx/" + path; 
   }
   
  @IPSJexlMethod(description="gets the failure image as a stream.", 
        params={})
  public InputStream getFailureImageStream() throws Exception
   {
      initServices();
      InputStream stream = null;
      String path = isdm.getFailureImagePath();
      try
      {
         stream = new FileInputStream(path);
      } catch (FileNotFoundException ex)
      {
         log.error("Configuration error: Unable to find failure image {}", path,
               ex.getMessage());
         log.debug(ex.getMessage(),ex);
         throw ex;
      }
      return stream;
   }
  
  @IPSJexlMethod(description="gets the failure image as a byte array.", 
        params={})
   public byte[] getFailureImageBinary() throws Exception
   {
      InputStream stream = this.getFailureImageStream();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PSCopyStream.copyStream(stream, baos);
      return baos.toByteArray(); 
   }
  
  @SuppressWarnings("unchecked")
   public static void logRequestAttributes(HttpServletRequest request, String message)
   {
      if(!log.isDebugEnabled())
         return;
      log.debug("logging request attributes for " + message);   
      Enumeration en = request.getAttributeNames();
      while(en.hasMoreElements())
      {
         String nm = (String) en.nextElement();
         log.debug("Attribute " + nm);
         Object val = request.getAttribute(nm);
         log.debug("Type is " + val.getClass().getCanonicalName());
         log.debug("Value is " + val); 
      } 
   }
   /**
    * Sets the isdm in unit tests. 
    * @param isdm the isdm to set 
    */
   public void setIsdm(ImageSizeDefinitionManager isdm)
   {
      this.isdm = isdm;
   }
   
   
}
