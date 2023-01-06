/**[ PSOptionManager.java ]*****************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class performes the following tasks:
 *
 * 1. Load the user options from the server.
 * 2. Get a named option value.
 * 3. Set a named option by modifying the existing one or by 
 *    creating a new one if does not already exist.
 * 4. Save the user settings to server for future use.
 * 5. DisplayOptions may also be retrieved explicitly
 *
 */
public class PSOptionManager
{
   /**
    * Creates a new instance of this object.
    *
    * @param defApplet - the applet which contains data needed to perform
    * the request that will be made to load and save the option.  Must not be
    * <code>null</code>.
    */
   public PSOptionManager(PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null or empty");

      m_applet = applet;
   }

   /**
    * Loads user settings from the server option handler. Server knows the user
    * name from the session. These settings include all properties overridden by
    * the user in the previous sessions.
    *
    * @throws PSContentExplorerException if there is a problem loading the
    * options.
    */
   public void load() throws PSContentExplorerException
   {
      try
      {
         //Loading user specified options
         Document doc = getUserOptionsDoc(false);
         setUserOptions(new PSUserOptions(doc.getDocumentElement()));

         //Loading default options
         doc = getUserOptionsDoc(true);
         setDefaultOptions(new PSUserOptions(doc.getDocumentElement()));
      }
      catch (Exception e)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.OPTIONS_LOAD_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Returns the <code>PSDisplayOptions</code>.
    *
    * @return may be <code>null</code> if the category "display" is
    * <code>null</code> or empty.
    */
   public PSDisplayOptions getDisplayOptions()
   {
      PSDisplayOptions theDisplayOptions = null;

      PSUserOptions puo = getUserOptions();

      if (puo == null){
         final JDialog dialog = new JDialog();
         dialog.setAlwaysOnTop(true);
         JOptionPane.showMessageDialog( dialog, m_applet.getResources().getString("userOption.missing.for.locale"), m_applet.getResources().getString("error"),
                 JOptionPane.ERROR_MESSAGE );
         System.exit(0);
      }else{
         PSOptions dispOptions =
                 puo.getOptions(PSDisplayOptions.DISPLAY_OPTIONS_CATEGORY);

         if (dispOptions != null)
            theDisplayOptions = new PSDisplayOptions(dispOptions);
      }

      return theDisplayOptions;
   }

   public PSOptions getCategoryOptions(String category)
   {
      return getUserOptions().getOptions(category);
   }
   
   public void addUserOptions(PSOptions options)
   {
      getUserOptions().addOptions(options);
   }

   /**
    * This toXml's all objects in the collection and appends the element
    * returned by the call to <code>el</code>. Collections must of type
    * <code>IPSClientObjects</code>, this simply iterates through the collection
    * calling toXml on each.
    *
    * @param el - on which to append the elements generated by the call to
    * each
    * {@link IPSClientObjects#toXml(Document) IPSClientObjects.toXml(Document)}
    * , must not be <code>null</code>.
    * @param doc - on which to create elements, must not be <code>null</code>.
    * @param col - must not be <code>null</code>, must contain objects of
    * <code>IPSClientObjects</code>, may be empty.
    */
   static void toXmlCollection(Element el, Document doc, Collection col)
   {
      if (el == null || doc == null || col == null)
         throw new IllegalArgumentException("arguments must not be null");

      Iterator it = col.iterator();
      IPSClientObjects comp = null;
      while (it.hasNext())
      {
         Object o = it.next();
         if (!(o instanceof IPSClientObjects))
            throw new IllegalArgumentException("Collection must contain only IPSClientObjects values");

         comp = (IPSClientObjects)o;
         el.appendChild(comp.toXml(doc));
      }
   }

   /**
    * Get the options document.
    */
   private Document getUserOptionsDoc(boolean loadDefault)
      throws IOException, SAXException, ParserConfigurationException
   {
      String appPath = 
         PSContentExplorerConstants.OPTIONS_URL
            + "?"
            + IPSHtmlParameters.SYS_COMMAND
            + "=";

      appPath += (loadDefault) ? LOAD_DEFAULT : LOAD_COMMAND;

      return m_applet.getXMLDocument(appPath);
   }

   /**
    * Saves user settings from memory to server using the server option handler.
    * Server knows the user name from the session. These settings include all
    * properties overridden by the user in the current sessions.
    * 
    * @param updateSessionTimeout <code>true</code> to have this request update the client session timeout on the
    * server, <code>false</code> to have this request avoid impacting the current session's client timeout.
    *
    * @throws PSContentExplorerException if an error occurs while trying to save the
    * options.
    */
   public void save(boolean updateSessionTimeout) throws PSContentExplorerException
   {
      if(m_applet.getActionManager() == null)
         return;
      
      try
      {
         Document postDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element el = getUserOptions().toXml(postDoc);
         postDoc.appendChild(el);

         String appPath =
               PSContentExplorerConstants.OPTIONS_URL
               + "?"
               + IPSHtmlParameters.SYS_COMMAND
               + "="
               + SAVE_COMMAND
               + "&"
               + "updateTimeout="
               + updateSessionTimeout;

         Map params = new HashMap();
         params.put(
            SESSIONOBJECT_CXOPTIONS,
            PSXmlDocumentBuilder.toString(postDoc));

         m_applet.getActionManager().postData(appPath, params);
      }
      catch (Exception e)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.OPTIONS_SAVE_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * @todo is there a utils package that has this?
    * Compares objects that implement the <code>equals()</code> method.
    * <code>String</code>s will be compared with case ignored.
    * Are they equal?
    *
    * @param a an object to compare
    * @param b an object to compare
    * @return <code>true</code>if they are, otherwise <code>false</code>.
    */
   static boolean compare(Object a, Object b)
   {
      if (a == null || b == null)
      {
         if (a != null || b != null)
            return false;
      }
      else
      {
         if (a.getClass().isArray() && b.getClass().isArray())
            return Arrays.equals((Object[])a, (Object[])b);
         if (a instanceof String && b instanceof String)
            return ((String)a).equalsIgnoreCase((String)b);
         if (!a.equals(b))
            return false;
      }
      return true;
   }

   /**
    * Gets the user options for this class.
    *
    * @return the useroptions of this object.  The user options are initialized
    * by the <code>load()</code>, and may be <code>null</code> if that method
    * was not called, but once initialized it is invariant.
    */
   private PSUserOptions getUserOptions()
   {
      return m_userOptions;
   }

   /**
    * Sets the user options for this class.
    *
    * @param userOptions the useroptions of this object.  Must not be
    * <code>null</code>.
    */
   private void setUserOptions(PSUserOptions userOptions)
   {
      if (userOptions == null)
         throw new IllegalArgumentException("userOptions must not be null");

      m_userOptions = userOptions;
   }

   /**
    * Gets the default user options for this class.
    *
    * @return the useroptions of this object.  The user options are initialized
    * by the <code>load()</code>, and may be <code>null</code> if that method
    * was not called, but once initialized it is invariant.
    */
   private PSUserOptions getDefaultOptions()
   {
      return m_defaultOptions;
   }

   /**
    * Returns the <code>PSDisplayOptions</code>.
    *
    * @return may be <code>null</code> if the category "display" is
    * <code>null</code> or empty.
    */
   public PSDisplayOptions getDefaultDisplayOptions()
      throws PSContentExplorerException
   {
      PSDisplayOptions theDisplayOptions = null;

      if (getDefaultOptions() == null)
         throw new IllegalStateException("load() must be called before calling getDefaultDisplayOptions()");

      PSOptions dispOptions =
         getDefaultOptions().getOptions(
            PSDisplayOptions.DISPLAY_OPTIONS_CATEGORY);

      if (dispOptions != null
         || dispOptions.getCategory().equals(
            PSDisplayOptions.DISPLAY_OPTIONS_CATEGORY))
         theDisplayOptions = new PSDisplayOptions(dispOptions);

      return theDisplayOptions;
   }

   /**
    * Sets the default user options for this class.
    *
    * @param userOptions the useroptions of this object.  Must not be
    * <code>null</code>.
    */
   private void setDefaultOptions(PSUserOptions userOptions)
   {
      if (userOptions == null)
         throw new IllegalArgumentException("userOptions must not be null");

      m_defaultOptions = userOptions;
   }

   /**
    * @see #PSOptionManager(PSContentExplorerApplet)
    * PSOptionManager(PSContentExplorerApplet)
    */
   private PSContentExplorerApplet m_applet = null;

   /**
    * @see #getUserOptions() getUserOptions()
    */
   private PSUserOptions m_userOptions = null;

   /**
    * @see #getDeafultOptions() getDefaultOptions()
    */
   private PSUserOptions m_defaultOptions = null;

   /**
    * This value is the key whose values are the persisted options in the
    * the session object.
    */
   public final static String SESSIONOBJECT_CXOPTIONS = "cxoptions";

   /**
    * This value is used to get the options from the session object, this
    * is the value of a {@link #IPSHTMLParameter.SYS_COMMAND } key.
    */
   public final static String LOAD_COMMAND = "get";

   /**
    * This value is used to explicitely get the default options from the system.
    * This is the value of a {@link #IPSHTMLParameter.SYS_COMMAND } key.
    */
   public final static String LOAD_DEFAULT = "default";

   /**
    * This value is used to put the options into the session object, this
    * is the value of a {@link #IPSHTMLParameter.SYS_COMMAND } key.
    */
   public final static String SAVE_COMMAND = "put";
}