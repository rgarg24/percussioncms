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

package com.percussion.deployer.error;

import com.percussion.conn.PSServerException;
import com.percussion.error.PSDeployException;
import com.percussion.error.PSDeployNonUniqueException;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for the PSDeployException class.
 */
public class PSDeployExceptionTest extends TestCase
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSDeployExceptionTest(String name)
   {
      super(name);
   }
   
   /**
    * Tests the Xml functions.
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      Object[] args1 = {"a", "b", "c"}; 
      PSDeployException ex1 = new PSDeployException(555, args1);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el1 = ex1.toXml(doc);
      PSDeployException ex2 = new PSDeployException(el1);
      
      assertEquals(ex1.getErrorCode(), ex2.getErrorCode());
      Object[] args2 = ex2.getErrorArguments();
      assertNotNull(args2);
      assertEquals(args1.length, args2.length);
      for (int i = 0; i < args1.length; i++) 
      {
         assertEquals(args1[i], args2[i]);
      }
      assertNull(ex2.getOriginalExceptionClass());
      
      Object[] args3 = {"a", "", "c"}; 
      PSServerException sEx1 = new PSServerException(123, args3);
      ex1 = new PSDeployException(sEx1);
      el1 = ex1.toXml(doc);
      ex2 = new PSDeployException(el1);
      assertEquals(ex1.getErrorCode(), ex2.getErrorCode());
      Object[] args4 = ex2.getErrorArguments();
      assertNotNull(args4);
      assertEquals(args3.length, args4.length);
      for (int i = 0; i < args3.length; i++) 
      {
         assertEquals(args3[i], args4[i]);
      }
      assertEquals(ex2.getOriginalExceptionClass(), sEx1.getClass().getName());
      
   }
   
   /**
    * Test the <code>PSDeployNonUniqueException</code> class
    * 
    * @throws Exception if there are any errors.
    */
   public void testNonUnique() throws Exception
   {
      Object[] args1 = {"a", "b", "c"}; 
      PSDeployNonUniqueException ex1 = new PSDeployNonUniqueException(555,
         args1);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el1 = ex1.toXml(doc);
      PSDeployNonUniqueException ex2 = new PSDeployNonUniqueException(el1);
      assertEquals(ex1.getErrorCode(), ex2.getErrorCode());
      assertEquals(ex1.getClass().getName(), ex2.getOriginalExceptionClass());
      
      boolean didThrow = false;
      try 
      {
         Object[] args3 = {"a", "", "c"}; 
         PSServerException sEx1 = new PSServerException(123, args3);
         ex1 = new PSDeployNonUniqueException(sEx1);
      }
      catch (UnsupportedOperationException ex) 
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }
     
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSDeployExceptionTest("testXml"));
      suite.addTest(new PSDeployExceptionTest("testNonUnique"));
      return suite;
   }
}
