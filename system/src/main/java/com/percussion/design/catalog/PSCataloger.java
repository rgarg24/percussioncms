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

package com.percussion.design.catalog;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.util.concurrent.ConcurrentHashMap;



/**
 * The PSCataloger class is used for the submission of catalog requests
 * against an E2 server.
 * <p>
 * To use the cataloger, a connection to the E2 server must first be
 * established. Requests can then be made through this object. This is
 * done by passing a set of properties to the <code>catalog</code>
 * method. This method analyzes the request and calls the appropriate
 * PSCatalogHandler extension. See the IPSCatalogHandler interface and
 * the classes implementing it for a list of the supported properties.
 * The catalog handler takes the input properties and creates a request
 * for the E2 server using the appropriate XML format. The server processes
 * the request, and returns the appropriate response in XML format. The
 * catalog handler takes the response and returns a Document object.
 * <p>
 * The mechanism for issuing catalog requests is described in the
 * {@link com.percussion.design.catalog com.percussion.catalog} package
 * description. By using the cataloger, building the requests is not
 * necessary. The following sample shows how to access back-end columns
 * for a particular table.
 * <pre><code>
 *      try {
 *         Properties connProps = new Properties();
 *         props.put("hostName", "myserver");
 *         props.put("loginId",  "myid");
 *         props.put("loginPw",  "mypw");
 *
 *         PSDesignerConnection   conn         = new PSDesignerConnection(connProps);
 *         PSCataloger            cataloger   = new PSCataloger(conn);
 *         Properties            props         = new Properties();
 *
 *         props.put("RequestCategory",  "data");
 *         props.put("RequestType",      "Column");
 *         props.put("DriverName",         "odbc");
 *         props.put("ServerName",         "MyServerDSN");
 *         props.put("TableName",         "mytab");
 *
 *         Document                  xmlDoc   = cataloger.catalog(props);
 *         PSCatalogResultsWalker  walker   = new PSCatalogResultsWalker(xmlDoc);
 *
 *         // Get data from the request info
 *         log.info("Table: " + walker.getRequestData("tableName"));
 *
 *         log.info("Column");
 *         log.info("------------------------------");
 *
 *         // now walk all the child objects (Column elements) to
 *         // get the column names
 *         while (walker.nextResultObject("Column")) {
 *            log.info(walker.getResultData("name"));
 *         }
 *      }
 *      catch (Exception e) {
 *         log.error(PSExceptionUtils.getMessageForLog(e));
 *      }
 *
 * </code></pre>
 * Which results in the following output:
 * <pre><code>
 *    Table: mytab
 *    Column
 *    ------------------------------
 *    mycol1
 *    mycol2
 *    mycol3
 * </code></pre>
 *
 * <P>
 * Sample code for a cataloger implementation can be found
 * <A HREF="../../../../../Testing/Catalog/E2CatalogRequestor.java">here</A>
 *
 * @see         IPSCatalogHandler
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSCataloger
{
   private static final Logger logger = LogManager.getLogger(PSCataloger.class);

   /**
    * Creates a cataloger connected to the specified E2 server.
    *
    * @param      conn                        the connection object for the
    *                                          desired E2 server
    */
   public PSCataloger(PSDesignerConnection conn)
   {
      super();

      if (conn == null)
         throw new IllegalArgumentException("conn obj null");

         m_conn = conn;

   }

   /**
    * Perform a catalog request against the connected E2 server.
    * <p>
    * The request properties must contain two properties which the
    * cataloger uses to determine which handler must be loaded. These
    * properties are:
    * <table border="2">
    * <tr><th>Key</th><th>Description</th></tr>
    * <tr><td>RequestCategory</td>
    *      <td>the catalog request category</td></tr>
    * <tr><td>RequestType</td>
    *      <td>the catalog request type within the category</td></tr>
    * </table>
    * <em>NOTE:</em> property keys and values are case sensitive
    * <p>
    * The <code>{@link com.percussion.design.catalog.data data}</code>
    * RequestCategory supports the following RequestType values:
    * <table border="2">
    * <tr><th>Value</th><th>Description</th></tr>
    * <tr><td>Driver</td>
    *      <td>used to locate the back-end drivers available for data access.
    *      </td></tr>
    * <tr><td>DriverSupport</td>
    *      <td>used to determine the functionality supported by a specific
    *         back-end driver.
    *      </td></tr>
    * <tr><td>Server</td>
    *      <td>used to locate the servers available through a back-end
    *         driver. Not all drivers are capable of locating servers.
    *      </td></tr>
    * <tr><td>Database</td>
    *      <td>used to locate the databases on the specified back-end server.
    *         Not all drivers/servers support databases.
    *      </td></tr>
    * <tr><td>Schema</td>
    *      <td>used to locate the schemas (AKA origins, owners) in the
    *         specified back-end database. Not all drivers/servers
    *         support schemas.
    *      </td></tr>
    * <tr><td>Table</td>
    *      <td>used to locate the tables in the specified back-end database.
    *      </td></tr>
    * <tr><td>Column</td>
    *      <td>used to locate the columns in the specified back-end table.
    *      </td></tr>
    * <tr><td>UniqueKey</td>
    *      <td>used to locate the column combinations which can be used to
    *         uniquely identify rows in the specified back-end table.
    *      </td></tr>
    * <tr><td>ForeignKey</td>
    *      <td>used to locate the columns which are related to other tables.
    *         Foreign key columns usually refer to the primary key of
    *         another table. This allows a unique relationship to be
    *         defined between the two tables. When a foreign key is
    *         defined, values cannot be inserted which do not exist in
    *         the table being referenced.
    *      </td></tr>
    * <tr><td>Index</td>
    *      <td>used to locate the indexes defined on a table. Indexes
    *         are used to sort data. This allows for faster access to
    *         the data. They can also be used to enforce unique column
    *         values.
    *      </td></tr>
    * </table>
    * <p>
    * The <code>{@link com.percussion.design.catalog.security security}</code>
    * RequestCategory supports the following RequestType values:
    *      ???
    * <p>
    * The <code>{@link com.percussion.design.catalog.exit exit}</code>
    * RequestCategory supports the following RequestType values:
    * <table border="2">
    * <tr><th>Value</th><th>Description</th></tr>
    * <tr><td>ServerExit</td>
    *      <td>provides the exit definition for all exits defined for global
    *      use on the server.
    *      </td></tr>
    * <tr><td>ServerUdfExit</td>
    *      <td>provides the UDF exit definition for all exits defined for
    *      global use on the server.
    *      </td></tr>
    * </table>
    * <p>
    * The <code>{@link com.percussion.design.catalog.mail mail}</code>
    * RequestCategory supports the following RequestType values:
    * <table border="2">
    * <tr><th>Value</th><th>Description</th></tr>
    * <tr><td>MailProvider</td>
    *      <td>get the mail providers available to the E2 server for sending
    *         e-mail messages.
    *      </td></tr>
    * </table>
    * <p>
    * The <code>{@link com.percussion.design.catalog.xml xml}</code>
    * RequestCategory supports the following RequestType values:
    * <table border="2">
    * <tr><th>Value</th><th>Description</th></tr>
    * <tr><td>DocType</td>
    *      <td>get the document types (DTDs) defined by the E2 server.
    *         These are the DTDs defined internally by the system, such
    *         as those used for cataloging purposes.
    *      </td></tr>
    * </table>
    *
    * <p>
    * If this object is not connected, an attempt will be made to
    * connect to the server.
    *
    * @param      req                        the request information
    *
    * @exception   PSServerException            if the server is not responding
    *
    * @exception   PSAuthorizationException    if design access to the server is
    *                                          denied
    *
    * @exception   java.io.IOException         if an I/O error occurs
    *
    * @see         IPSCatalogHandler
    */
   public Document catalog(java.util.Properties req)
      throws   PSServerException,
               PSAuthenticationFailedException,
               PSAuthorizationException, java.io.IOException
   {
      logger.info("Performing catalog: {}" , req);

      /* check the request type to determine which handler to use */
      String reqCategory   = (String)req.get("RequestCategory");
      String reqType         = (String)req.get("RequestType");

      if (reqCategory == null) {
         throw new IllegalArgumentException("reqd prop not specified: RequestCategory");
      }
      else if (reqType == null) {
         throw new IllegalArgumentException("reqd prop not specified: RequestType");
      }

      /* let handler format the request (throws exception if it can't) */
      IPSCatalogHandler handler = getHandler(reqCategory, reqType);

      /* format the request as an XML document */
      Document sendDoc = handler.formatRequest(req);

      /* send the request to the server */
      Document respDoc;


      synchronized (m_conn) {
         m_conn.setRequestType("design-catalog-" + reqCategory);
         respDoc = m_conn.execute(sendDoc);
      }


      return respDoc;
   }

   private IPSCatalogHandler getHandler(   java.lang.String reqCategory,
                                          java.lang.String reqType)
   {
      IPSCatalogHandler handler = null;

      if (m_Handlers != null) {
         handler = m_Handlers.get(
            reqCategory + "-" + reqType);
      }
      else {
         m_Handlers = new ConcurrentHashMap<>();
      }

      if (handler == null) {
         try {
            Class[] noClass = {};
            Object[] noObjects = {};
            Class hClass = Class.forName(   "com.percussion.design.catalog." +
               reqCategory + ".PS" + reqType + "CatalogHandler" );
            handler = (IPSCatalogHandler)hClass.getConstructor(noClass).
               newInstance(noObjects);
            m_Handlers.put(reqCategory + "-" + reqType, handler);
         } catch (Exception e) {
            throw new IllegalArgumentException("req handler exception: " +
                reqCategory + " " + reqType + " " +  e.toString());
         }
      }

      return handler;
   }

   public String prepareCredentials(String uid, String pw)
   {
      try {
         return PSEncryptor.encryptString(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR),pw);
      } catch (PSEncryptionException e) {
         logger.error("Error encrypting password: {}", PSExceptionUtils.getMessageForLog(e));
         logger.debug(e);
         return "";
      }

   }



   private volatile PSDesignerConnection      m_conn;

   private static ConcurrentHashMap<String,IPSCatalogHandler>         m_Handlers = null;
}

