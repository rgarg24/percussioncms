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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Adds the desing Display Format columns to be used by List View services (under design node)
 * @author federicoromanelli
 *
 */
public class PSUpgradePluginAddDesignDisplayFormatValues implements IPSUpgradePlugin
{

   private PrintStream logger;
   
   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   private Properties m_dbProps = null;
   
   
   /* (non-Javadoc)
    * @see com.percussion.install.IPSUpgradePlugin#process(com.percussion.install.IPSUpgradeModule, org.w3c.dom.Element)
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule module, @SuppressWarnings("unused") Element elemData)
   {
      logger = module.getLogStream();
      Connection conn = null;
      
      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);
         fixDisplayFormat(conn);
         fixDisplayFormatColumns(conn);
         // TODO: check if this should be here
         fixDefaultDisplayFormatWidths(conn);
      }
      catch(Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e.getLocalizedMessage());
      }
      finally
      {
         if(conn != null)
            try
            {
               conn.close();
            }
            catch (SQLException se)
            {
               return new PSPluginResponse(PSPluginResponse.EXCEPTION, se.getLocalizedMessage());
            }
      }
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }
   
   /**
    * Checks if the default display format is already in the database.
    * If it is, it set the displayFormatId with the current value.
    * If it's not, it inserts the corresponding row in table PSX_DISPLAYFORMATS 
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixDisplayFormat(Connection conn) throws SQLException
   {
      String displayFormatTable = qualifyTableName("PSX_DISPLAYFORMATS");
      logger.println("Searching existing Display Format for CM1_Design");
      String query = "Select DISPLAYID FROM " +
         displayFormatTable +
         " WHERE INTERNALNAME LIKE '%CM1_Design%'";
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      boolean updated = false;
      
      if (!results.next())
      {
         String queryLastId = "Select max(DISPLAYID) as DISPLAYID FROM " + displayFormatTable;
         PreparedStatement psLastId = conn.prepareStatement(queryLastId);
         ResultSet resultsLastId = psLastId.executeQuery();
         if (resultsLastId.next())
         {
            displayFormatId = resultsLastId.getInt("DISPLAYID") + 1;
         
            logger.println("Display Format not found, inserting corresponding row with id " + displayFormatId);
            String insertDisplayFormatQuery =
               "INSERT INTO " + displayFormatTable + "(DISPLAYID, INTERNALNAME, DISPLAYNAME, DESCRIPTION, VERSION)" +
               " VALUES(?,?,?,?,?)";
            
            PreparedStatement ps2 = conn.prepareStatement(insertDisplayFormatQuery);
            ps2.setInt(1, displayFormatId);                         // DISPLAYID
            ps2.setString(2, "CM1_Design");                         // INTERNALNAME
            ps2.setString(3, "Design");                             // DISPLAYNAME
            ps2.setString(4, "Design List View display format.");   // DESCRIPTION
            ps2.setInt(5, 0);                                       // VERSION
            ps2.executeUpdate();
            updated = true;
         }
      }
      else
      {
         displayFormatId = results.getInt("DISPLAYID");
         logger.println("Display Format found, updating display format id with: " + displayFormatId);         
      }

      if(updated)
         conn.commit();
   }

   /**
    * Updates the default CM1 display format width column values 
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixDisplayFormatColumns(Connection conn) throws SQLException
   {
      if (displayFormatId == -1)
         return;
      String displayFormatColumnsTable = qualifyTableName("PSX_DISPLAYFORMATCOLUMNS");
      logger.println("Searching existing Display Format Columns for CM1_Design");
      String query = "Select SOURCE FROM " +
         displayFormatColumnsTable +
         " WHERE DISPLAYID = ?";
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setInt(1, displayFormatId);
      ResultSet results = ps.executeQuery();
      boolean updated = false;

      if (!results.next())
      {
         logger.println("Display Format Columns not found, inserting corresponding rows for fields" +
                 " sys_title, sys_size, sys_contentlastmodifieddate");
         String insertDisplayFormatColumnQuery =
            "INSERT INTO " + displayFormatColumnsTable +
            "(DISPLAYID, SOURCE, DISPLAYNAME, TYPE, RENDERTYPE, SORTORDER, SEQUENCE, DESCRIPTION, WIDTH)" +
            " VALUES(?,?,?,?,?,?,?,?,?)";
         
         PreparedStatement ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);                // DISPLAYID
         ps2.setString(2, "sys_title");                 // SOURCE
         ps2.setString(3, "Name");                      // DISPLAYNAME
         ps2.setInt(4, 0);                              // TYPE
         ps2.setString(5, "Text");                      // RENDERTYPE
         ps2.setString(6, "A");                         // SORTORDER
         ps2.setInt(7, 0);                              // SEQUENCE
         ps2.setString(8, "The name of the item.");     // DESCRIPTION
         ps2.setString(9, null);                         // WIDTH
         ps2.executeUpdate();
         
         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_size");
         ps2.setString(3, "Size");
         ps2.setInt(4, 0);
         ps2.setString(5, "Number");
         ps2.setString(6, "A");
         ps2.setInt(7, 1);
         ps2.setString(8, "The item size.");
         ps2.setInt(9, DEFAULT_WIDTH);
         ps2.executeUpdate();

         ps2 = conn.prepareStatement(insertDisplayFormatColumnQuery);
         ps2.setInt(1, displayFormatId);
         ps2.setString(2, "sys_contentlastmodifieddate");
         ps2.setString(3, "Modified");
         ps2.setInt(4, 0);
         ps2.setString(5, "Date");
         ps2.setString(6, "A");
         ps2.setInt(7, 2);
         ps2.setString(8, "The item's last modified date.");
         ps2.setInt(9, DEFAULT_WIDTH);
         ps2.executeUpdate();
         
         updated = true;         
      }

      if(updated)
         conn.commit();
   }

   /**
    * Checks if the default display format is already in the database.
    * If it is, it set the displayFormatId with the current value.
    * If it's not, it inserts the corresponding row in table PSX_DISPLAYFORMATS 
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixDefaultDisplayFormatWidths(Connection conn) throws SQLException
   {
      String displayFormatTable = qualifyTableName("PSX_DISPLAYFORMATS");
      String displayFormatColumnsTable = qualifyTableName("PSX_DISPLAYFORMATCOLUMNS");
      logger.println("Searching existing Display Format for CM1_Default");
      String query = "Select DISPLAYID FROM " +
         displayFormatTable +
         " WHERE INTERNALNAME LIKE '%CM1_Default%'";
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      boolean updated = false;
      if (results.next())
      {
         int defaultDisplayFormatId = results.getInt("DISPLAYID");
         
         String updateDesignDisplayFormatQuery =
            "UPDATE " + displayFormatColumnsTable + " set WIDTH = ? " +
            " WHERE DISPLAYID = ?" +
            " AND SEQUENCE > ?";
         
         PreparedStatement ps2 = conn.prepareStatement(updateDesignDisplayFormatQuery);
         ps2.setInt(1, DEFAULT_WIDTH);
         ps2.setInt(2, defaultDisplayFormatId);
         ps2.setInt(3, 0);
         ps2.executeUpdate();
         updated = true;
      }

      if(updated)
         conn.commit();
   }   
   /**
    * This will create a fully qualified table name. Depending on the provided
    * driver type we will return table, owner.table or db.owner.table.
    * 
    * @param table the table name to qualify, must be valid
    */
   private String qualifyTableName(String table)
   {
      String database = m_dbProps.getProperty("DB_NAME");
      String schema = m_dbProps.getProperty("DB_SCHEMA");
      String driver = m_dbProps.getProperty("DB_DRIVER_NAME");

      return PSSqlHelper.qualifyTableName(table, database, schema, driver);
   }
   
   // The default display format id (it's value is updated in method fixDisplayFormat if needed)
   private int displayFormatId = -1;
   public static final int DEFAULT_WIDTH = 80; 
}
