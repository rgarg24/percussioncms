/******************************************************************************
 *
 * [ PSDeliveryTierDatabaseConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAConsoleUtils;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.PSDeliveryTierDatabaseModel;
import com.percussion.installer.model.PSDeliveryTierProtocolModel;


/**
 * Console Implementation of {@link PSDeliveryTierDatabaseModel}.
 */
public class PSDeliveryTierDatabaseConsole extends RxIAConsole
{
   @Override
   public void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      PSDeliveryTierDatabaseModel dM = getDM();

      RxIAConsoleUtils cu = getConsoleUtils();
      String prompt = "Enter one of the options above";
      
      String[] schemas = dM.getSchemas();
      if (schemas != null && schemas.length > 0)
      {
         // Determine the index of the currently selected option
         String strSchema = (String) getDM().getPropertyValue(
               PSDeliveryTierProtocolModel.SCHEMA_NAME);
         int schemaIndex = getItemIndex(schemas, strSchema);

         String propKey = dM.isMySql() ? "database" : "schema";
         cu.wprintln(RxInstallerProperties.getString(propKey));
         int res1 = cu.createChoiceListAndGetValue(prompt, schemas, schemaIndex);
         dM.setSchema(schemas[res1]);
      }

      String[] dbs = dM.getDatabases();
      if (dbs != null && dbs.length > 0)
      {
         // Determine the index of the currently selected option
         String strDatabase = (String) getDM().getPropertyValue(
               PSDeliveryTierProtocolModel.DB_NAME);
         int dbIndex = 0;

         if (strDatabase != null)
            dbIndex = getItemIndex(dbs, strDatabase);
               
         cu.wprintln(RxInstallerProperties.getString("database"));
         int res = cu.createChoiceListAndGetValue(prompt, dbs, dbIndex);
         dM.setDatabase(dbs[res]);
      }
      
      // prompt for datasource configuration name
      String dsconfig = cu.promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getString("dsconfig"), getDM().getDSConfig());
      dM.setDSConfig(dsconfig);
   }

   @Override
   protected void initialize()
   {
      super.initialize();
      
      PSDeliveryTierDatabaseModel dm = new PSDeliveryTierDatabaseModel(this);
      setModel(dm);
   }
   
   /**
    * Helper method to access the model i.e. retrieve {@link RxDatabaseModel}.
    * 
    * @return the data model for this console.
    */
   private PSDeliveryTierDatabaseModel getDM()
   {
      return (PSDeliveryTierDatabaseModel) getModel();
   }
}
