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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link PSRelationshipConfig} class.
 */
public class PSRelationshipConfigTest
{
   /**
    * The all public constructor contracts.
    * 
    * @throws Exception for any error.
    */
   @Test
   public void testConstructors() throws Exception
   {
      // test constructor 1: all valid parameters
      Exception exception = null;
      try
      {
         new PSRelationshipConfig("name", 
            PSRelationshipConfig.RS_TYPE_SYSTEM);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);
         
      // test constuctor 1: null name
      exception = null;
      try
      {
         new PSRelationshipConfig(null, 
            PSRelationshipConfig.RS_TYPE_SYSTEM);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
         
      // test constuctor 1: empty name
      exception = null;
      try
      {
         new PSRelationshipConfig(" ", 
            PSRelationshipConfig.RS_TYPE_SYSTEM);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
         
      // test constructor 1: name with space characters
      exception = null;
      try
      {
         new PSRelationshipConfig("name with space",
               PSRelationshipConfig.RS_TYPE_USER);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      exception = null;
      try
      {
         new PSRelationshipConfig("namewith\tTAB",
               PSRelationshipConfig.RS_TYPE_USER);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test constuctor 1: null type
      exception = null;
      try
      {
         new PSRelationshipConfig("name", null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
         
      // test constuctor 1: empty type
      exception = null;
      try
      {
         new PSRelationshipConfig("name", " ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test constuctor 1: invalid type
      exception = null;
      try
      {
         new PSRelationshipConfig("name", "foo");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      PSRelationshipConfigSet configs = getConfigs();
      PSRelationshipConfig newCopy = configs
            .getConfig(PSRelationshipConfig.TYPE_NEW_COPY);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      // test constuctor 2: all valid parameters
      exception = null;
      try
      {
         new PSRelationshipConfig(newCopy.toXml(doc), null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);
      
      // test constuctor 2: null document
      exception = null;
      try
      {
         new PSRelationshipConfig(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof PSUnknownNodeTypeException);
   }
   
   /**
    * Get the relationship configuration set used for testing purposes.
    * 
    * @return the relationship configuration set, never <code>null</code> or
    *    empty.
    * @throws Exception for any error reading the relationship configuration
    *    set.
    */
   public static PSRelationshipConfigSet getConfigs() throws Exception
   {
      Element configXml = loadXmlResource(
                      "/com/percussion/testing/relationshipConfigurations.xml", PSRelationshipConfigTest.class);

      return new PSRelationshipConfigSet(configXml, null, null);
   }
   
   /**
    * Loads an XML document from a path that is relative to the suppliedt class.
    * 
    * @param path the relative path to the specified class, it may not be 
    *    <code>null</code> or empty.
    * @param cz the class that the above path is relative to, never 
    *    <code>null</code>.
    * 
    * @return the root element of the document, never <code>null</code>.
    */
   public static Element loadXmlResource(String path, Class cz) throws Exception
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty.");
      if (cz == null)
         throw new IllegalArgumentException("cz may not be null.");
      
      InputStream in = cz.getResourceAsStream(path);
      if (in == null)
      {
         throw new FileNotFoundException(
               "Resource \"" + path + "\" was not found from " + cz);
      }
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      in.close();
      return doc.getDocumentElement();
   }
   

   /**
    * Test public API contracts.
    * 
    * @throws Exception for all errors.
    */
   @Test
   public void testPublicAPI() throws Exception
   {
      PSRelationshipConfigSet configs = getConfigs();
      PSRelationshipConfig config = configs
            .getConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY);

      Exception exception = null;
      try
      {
         config.copyFrom(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.getProcessCheck(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.getProcessCheck(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.getProperty(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.getSysProperty(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.getSystemProperty(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.getUsrProperty(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.setEffects(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.setLabel(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.setLabel(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.setName(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.setName(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.setProcessChecks(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         config.setUserDefProperties(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
   }
   
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   @Test
   public void testEqualsHashCode()
   {
      final PSRelationshipConfig config = new PSRelationshipConfig("name",
         PSRelationshipConfig.RS_TYPE_SYSTEM);
      assertFalse(config.equals(new Object()));
      assertEqualsWithHash(config, new PSRelationshipConfig("name",
         PSRelationshipConfig.RS_TYPE_SYSTEM));
      assertFalse(config.equals(new PSRelationshipConfig("name1",
         PSRelationshipConfig.RS_TYPE_SYSTEM)));
   }
}
