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
package com.percussion.security;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSUserManagementEvent;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSSubject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.callback.CallbackHandler;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * The PSBackEndTableProvider class uses a JDBC (back-end) table as a user
 * directory. The user's name and password are stored in the table, which are
 * used to authenticate the user.
 */
public class PSBackEndTableProvider extends PSSecurityProvider
{

   private static final Logger log = LogManager.getLogger(PSBackEndTableProvider.class);

   /**
    * Construct an instance of this provider.  If a password filter class
    *    is specified in the properties, then it is assumed it will be 
    *    accessible for us to load.  
    *
    * @param props see 
    * {@link PSBackEndConnection#PSBackEndConnection(Properties)} for a 
    * description.
    * @param providerInstance the name of this provider instance,
    *    never <code>null</code>.
    */
   PSBackEndTableProvider(Properties props, String providerInstance)
   {
      super(SP_NAME, providerInstance);

      if (props == null)
         throw new IllegalArgumentException("Null properties not allowed!");

      if (providerInstance == null)
         throw new IllegalArgumentException(
               "Null provider instance not allowed!");

      m_backendConnection = new PSBackEndConnection(props);
      m_defaultDirectoryProvider = new PSProvider(
         PSBackEndTableDirectoryCataloger.class.getName(),
         PSProvider.TYPE_DIRECTORY, null); 
      m_dirCataloger = new PSBackEndTableDirectoryCataloger(props);
   }

   /**
    * Write an event to the audit log on
    * @param uid The user id
    * @param action The activity taken
    * @param activityMsg The action taken
    */
   private void auditlogUserActivity(String uid, PSUserManagementEvent.UserEventActions action, String activityMsg){

        PSAuditLogService auditLogService = PSAuditLogService.getInstance();

        HttpServletRequest httpRequest = PSThreadRequestUtils.getPSRequest().getServletRequest();

        PSUserManagementEvent event = new PSUserManagementEvent(
                httpRequest,
                action,
                PSActionOutcome.SUCCESS
        );
        event.setTargetUsername(uid);
        event.setIniatorName("system");
        event.setActivity(activityMsg);
        auditLogService.logUserManagementEvent(event);

   }
   /**
    * Authenticate a user with the specified credentials. If a connection can
    * be made to the table and the uid can be found with the corresponding
    * password, the authentication is considered successful.
    *
    * @see IPSSecurityProvider
    */
   public PSUserEntry authenticate(String uid, String pw, 
      CallbackHandler callbackHandler)
      throws PSAuthenticationFailedException
   {

      // fail if null uid      
      if (uid == null)
      {
         throw new PSAuthenticationFailedException(
            SP_NAME, m_spInstance, "null");         
      }

      //If not Clear text password
      //incase password is copied from db and passed in, then they should not be authenticated
      //user must pass in password in plain text.
      // Just an assumption that plain pwd string can't be longer than 150 chars
      // if it is longer than 150, means could be copied from db.
      if (pw.length() > 150) {
         throw new PSAuthenticationFailedException(
                 SP_NAME, m_spInstance, uid);
      }

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet result = null;
      try
      {
         conn = m_backendConnection.getDbConnection();
         stmt = m_backendConnection.getPreparedStatement(uid, conn);
         result = stmt.executeQuery();

         // no user match if no rows are returned
         if (!result.next())
            throw new PSAuthenticationFailedException(
               SP_NAME, m_spInstance, uid);

         // get the password from the current row
         String password = result.getString(
            m_backendConnection.getPasswordColumn());
         
         // rhythmyx user must be unique for authentication
         if (result.next())
         {
            Object[] args = { m_spInstance, uid };

            throw new PSAuthenticationFailedException(
               IPSSecurityErrors.BETABLE_ERROR_UID_NOT_UNIQUE, args);
         }

         // check that the password matches
         IPSPasswordFilter filter = m_backendConnection.getPasswordFilter();
         String encodedPw  = pw;
         boolean authenticationValid = false;
         if (filter != null) {

               authenticationValid = PSPasswordHandler.checkHashedPassword(pw, password);
            if (!authenticationValid) {
               //Check if it is encrypted with the legacy algorithm
               encodedPw = filter.legacyEncrypt(pw);
               if (!encodedPw.equals(password)) {
                  authenticationValid = false;
               } else {
                  authenticationValid = true;

                  log.info("Security Update: Re-encrypting password for database user: {} from legacy algorithm {} to current algorithm {}",
                          uid,
                          filter.getLegacyAlgorithm(),
                          filter.getAlgorithm());

                  //The password needs re-encrypted with the filters new algorithm.
                  m_backendConnection.updateUserPassword(uid, filter.encrypt(pw));
                  auditlogUserActivity(uid,
                          PSUserManagementEvent.UserEventActions.update,
                          String.format("Security Update: Re-encrypting password for database user: {%s} from legacy algorithm {%s} to current algorithm {%s}",
                                  uid,
                                  filter.getLegacyAlgorithm(),
                                  filter.getAlgorithm()));
               }
            }
         }

         if (!authenticationValid) {
            //Clear text password
            if (!pw.equals(password)) {
               throw new PSAuthenticationFailedException(
                       SP_NAME, m_spInstance, uid);
            }else{
               authenticationValid = true;

               if(filter != null) {
                  log.info("Security Update: Re-encrypting password for database user: {} from legacy algorithm: {} to current algorithm: {}",
                          uid,
                          filter.getLegacyAlgorithm(),
                          filter.getAlgorithm());

                  //The password needs re-encrypted with the filters new algorithm.
                  m_backendConnection.updateUserPassword(uid, filter.encrypt(pw));
                  auditlogUserActivity(uid,
                          PSUserManagementEvent.UserEventActions.update,
                          String.format("Security Update: Re-encrypting password for database user: {%s} from legacy algorithm: {%s} to current algorithm: {%s}",
                                  uid,
                                  "Plain Text",
                                  filter.getAlgorithm()));
               }
            }
         }

         // get user attributes
         PSSubject subject = m_dirCataloger.getAttributes(uid, null);
         PSUserAttributes attributeValues = null;
         PSAttributeList attributes = subject.getAttributes();
         if (attributes != null && attributes.size() > 0)
            attributeValues = new PSUserAttributes(attributes);

         return new PSUserEntry(uid, 0, null, attributeValues, PSUserEntry
            .createSignature(uid, pw));
         }
      catch (SQLException | PSEncryptionException e)
         {
         throw new PSAuthenticationFailedException(
            SP_NAME, m_spInstance, uid, e.toString());
               }
      finally
         {
         if (result != null)
            try { result.close(); } catch (SQLException e) { /* noop */ }

         if (stmt != null)
            try { stmt.close(); } catch (SQLException e) { /* noop */ }

         if (conn != null)
            try { conn.close(); } catch (SQLException e) { /* noop */ }
            }
         }

   /** @see IPSSecurityProvider */
   public IPSSecurityProviderMetaData getMetaData()
   {
      if (m_metaData == null)
            {
         String[] attrNames = null;

         List attributeNamesList = new ArrayList();
         Iterator attributeNames = m_backendConnection.getUserAttributeNames();
         while (attributeNames.hasNext())
            attributeNamesList.add(attributeNames.next());

         if (!attributeNamesList.isEmpty())
         {
            attrNames = (String[]) attributeNamesList
                  .toArray(new String[attributeNamesList.size()]);
         }

         m_metaData = new PSBackEndTableProviderMetaData(this,
            m_backendConnection.getUserColumn(), m_backendConnection.getTable(),
            attrNames);
      }

      return m_metaData;
   }

   /** @see PSSecurityProvider */
   public PSProvider getDefaultDirectoryProvider()
   {
      return m_defaultDirectoryProvider; 
   }

   /**
    * Return a connection from the database pool for this backend connection.
    *
    * @return the connection to the database, never <code>null</code>.
    * @throws SQLException for any error getting the connection.
    */
   public Connection getDbConnection() throws SQLException
   {
      return m_backendConnection.getDbConnection();
   }

   /**
    * The name of this security provider.
    */
   public static final String SP_NAME = "BackEndTable";

   /**
    * The class name of this security provider.
    */
   public static final String SP_CLASSNAME = 
      PSBackEndTableProvider.class.getName();

   /**
    * The backend connection used to authenticate users and lookup user
    * attributes. Initialized in constructor, never <code>null</code> or
    * changed after that.
    */
   private PSBackEndConnection m_backendConnection = null;
   
   /**
    * Default directory provider, initialized during ctor, never 
    * <code>null</code> or modified after that.
    */
   private PSProvider m_defaultDirectoryProvider;
   
   /**
    * Default directory cataloger, initialized during ctor, never 
    * <code>null</code> or modified after that.
    */
   private IPSDirectoryCataloger m_dirCataloger;
}
