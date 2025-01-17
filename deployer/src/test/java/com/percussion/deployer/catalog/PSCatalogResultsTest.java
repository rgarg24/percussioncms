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

package com.percussion.deployer.catalog;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test class for the <code>PSDbmsInfo</code> class.
 */
public class PSCatalogResultsTest extends TestCase
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSCatalogResultsTest(String name)
   {
      super(name);
   }
   
   /**
    * Test constructing all catalog result classes (<code>PSCatalogResultColumn
    * </code>, <code>PSCatalogResult</code> and <code>PSCatalogResultSet</code>)
    * 
    * @throws Exception If there are any errors.
    */
   public void testConstructors() throws Exception
   {
      //Test PSCatalogResultColumn constructors      
      // Should work fine
      assertTrue(testCatalogResultColumnCtor("Name", 
         PSCatalogResultColumn.TYPE_TEXT));
      
      //empty column name, should fail      
      assertTrue(!testCatalogResultColumnCtor("", 
         PSCatalogResultColumn.TYPE_TEXT));
      
      //invalid type
      assertTrue(!testCatalogResultColumnCtor("Name", 5));     
      
      //Test PSCatalogResult constructors      
      // Should work fine
      assertTrue(testCatalogResultCtor(
         "Content Editors", "Content Editors"));
      
      //null id, should fail      
      assertTrue(!testCatalogResultCtor(null, "Content Editors"));
      
      //null displaytext, should fail
      assertTrue(!testCatalogResultCtor("Content Editors", null));   
      
      
      //Test PSCatalogResultSet, should construct empty results by default
      PSCatalogResultSet resultSet = new PSCatalogResultSet();
      assertTrue(!resultSet.getResults().hasNext());
   }
   
   /**
    * Tests constructing the <code>PSCatalogResultColumn</code> object. Please
    * see {@link #PSCatalogResultColumn(String, int)} for parameter description.
    * 
    * @return <code>true</code> if the object is constructed successfully 
    * without an exception, otherwise <code>false</code>
    */
   private boolean testCatalogResultColumnCtor(String name, int type)
   {      
      try {
         PSCatalogResultColumn resultColumn = new PSCatalogResultColumn(
            name, type);
      }
      catch(Exception e)
      {
         return false;
      }
      
      return true;
   }
   
   /**
    * Tests constructing the <code>PSCatalogResult</code> object. Please
    * see {@link #PSCatalogResult(String, String, Object[])} for parameter 
    * description.
    * 
    * @return <code>true</code> if the object is constructed successfully 
    * without an exception, otherwise <code>false</code>
    */   
   private boolean testCatalogResultCtor(String id, String dispText)
   {
      try {
         PSCatalogResult result = new PSCatalogResult(id, dispText);
      }
      catch(Exception e)
      {
         return false;
      }
      
      return true;
   }
   
   /**
    * Tests all methods defined in <code>IPSDeployComponent</code> interface.
    * 
    * @throws Exception if there are any errors.
    */
   public void testInterfaceMethods() throws Exception
   {
      List columns = new ArrayList();
      columns.add(new PSCatalogResultColumn(
         "Name", PSCatalogResultColumn.TYPE_TEXT));
      columns.add(new PSCatalogResultColumn(
         "Number", PSCatalogResultColumn.TYPE_NUMERIC));  
      columns.add(new PSCatalogResultColumn(
         "Created Date", PSCatalogResultColumn.TYPE_DATE));                            
      
      PSCatalogResultColumn[] columnMeta = 
         (PSCatalogResultColumn[])columns.toArray(new PSCatalogResultColumn[0]);
      PSCatalogResultSet resultSet = new PSCatalogResultSet(columnMeta);
      
      PSCatalogResult result = 
         new PSCatalogResult("ArchiveLogID1", "testArchive1");
      result.addTextColumn("testArchive1");
      result.addNumericColumn(5);
      result.addDateColumn(System.currentTimeMillis());
      resultSet.addResult(result);      
      
      result = new PSCatalogResult("ArchiveLogID2", "testArchive2");
      result.addTextColumn("testArchive2");
      result.addNumericColumn(6);
      result.addDateColumn(System.currentTimeMillis());
      resultSet.addResult(result);  

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = resultSet.toXml(doc);
      PSCatalogResultSet otherSet =  new PSCatalogResultSet(srcEl);
      assertTrue(resultSet.equals(otherSet));
      
      PSCatalogResult oneMore = 
         new PSCatalogResult("ArchiveLogID3", "testArchive3");
      oneMore.addTextColumn("testArchive3");
      oneMore.addNumericColumn(7);
      oneMore.addDateColumn(System.currentTimeMillis());         
      resultSet.addResult(oneMore);
      assertTrue(!resultSet.equals(otherSet));
      
      otherSet.copyFrom(resultSet);
      
      assertTrue(resultSet.equals(otherSet));
      assertTrue(resultSet.hashCode() == otherSet.hashCode());
      
      //The result column objects are not in the order of column meta
      //so the validation should return false
      PSCatalogResult testWrongColumns = 
         new PSCatalogResult("ArchiveLogID3", "testArchive3");
      testWrongColumns.addDateColumn(System.currentTimeMillis());                  
      testWrongColumns.addTextColumn("testArchive3");
      testWrongColumns.addNumericColumn(7);      
      assertTrue(!resultSet.validateResultToAdd(testWrongColumns));
      
      //Tests validation to add a result with columns to the result set which
      //does not support columns.
      PSCatalogResultSet set = new PSCatalogResultSet();
      //should fail as result has columns but set does not support
      assertTrue(!set.validateResultToAdd(oneMore));
   }   
   
   
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSCatalogResultsTest("testConstructors"));
      suite.addTest(new PSCatalogResultsTest("testInterfaceMethods"));
      return suite;
   }
   
}
