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

package com.percussion.error;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Abstract exception class to used to report general exceptions for standalone
 * applications.  Handles formatting of messages stored in the resource bundle
 * (supplied by derived classes) using error codes and arguments. Localization
 * is also supported.
 */
public abstract class PSStandaloneException extends Exception
{

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param singleArg The argument to use as the sole argument in
    *    the error message, may be <code>null</code>.
    */
   public PSStandaloneException(int msgCode, Object singleArg)
   {
      this(msgCode, new Object[] { singleArg });
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    */
   public PSStandaloneException(int msgCode, Object[] arrayArgs)
   {
      init(msgCode, arrayArgs);
   }

   /**
    * Initialize the exception with the code and arguments 
    * @param msgCode the code of the exception
    * @param arrayArgs the arguments, may be null
    */
   private void init(int msgCode, Object[] arrayArgs)
   {
      for (int i = 0; arrayArgs != null && i < arrayArgs.length; i++)
      {
         if (arrayArgs[i] == null)
            arrayArgs[i] = "";
      }

      m_code = msgCode;
      m_args = arrayArgs;
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode The error string to load.
    */
   public PSStandaloneException(int msgCode)
   {
      init(msgCode, null);
   }

   /**
    * Construct an exception from a class derived from PSException.  The name of
    * the original exception class is saved.
    *
    * @param ex The exception to use.  Its message code and arguments are stored
    * along with the original exception class name.  May not be
    * <code>null</code>.
    */
   public PSStandaloneException(PSException ex)
   {
      super(ex);
      init(ex.getErrorCode(), ex.getErrorArguments());
      m_originalExceptionClass = ex.getClass().getName();
   }

   /**
    * Construct an exception from a class PSStandaloneException.  The name of
    * the original exception class is saved. This is a convenient constructor
    * used by derived classes.
    *
    * @param ex The exception to use.  Its message code and arguments are stored
    * along with the original exception class name.  May not be
    * <code>null</code>.
    */
   protected PSStandaloneException(PSStandaloneException ex)
   {
      super(ex);
      init(ex.getErrorCode(), ex.getErrorArguments());
      m_originalExceptionClass = ex.getClass().getName();
   }

   /**
    * Construct an exception from its XML representation.
    *
    * @param source The root element of this object's XML representation.
    * Format expected is defined by the {@link #toXml(Document) toXml} method
    * documentation.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by the class.
    */
   public PSStandaloneException(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      if (!getXmlNodeName().equals(source.getNodeName()))
      {
         Object[] args = { getXmlNodeName(), source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      // get message code
      String sTemp = null;
      sTemp = source.getAttribute(XML_ATTR_MSG_CODE);
      try
      {
         m_code = Integer.parseInt(sTemp);
      }
      catch (NumberFormatException e)
      {
         Object[] args = { getXmlNodeName(), XML_ATTR_MSG_CODE, sTemp == null ?
            "null" : sTemp };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);

      }

      // get optional exception class
      sTemp = source.getAttribute(XML_ATTR_EXCEPTION_CLASS);
      if (sTemp != null && sTemp.trim().length() > 0)
         m_originalExceptionClass = sTemp;

      // get args
      List argList = new ArrayList();
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      Element arg = tree.getNextElement(XML_ELEMENT_ARG,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (arg != null)
      {
         argList.add(tree.getElementData());
         arg = tree.getNextElement(XML_ELEMENT_ARG,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      m_args = argList.toArray();
   }

   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. The format is:
    *
    * <pre><code>
    * <!ELEMENT PSXLoaderException (Arg*)
    * <!ATTLIST PSXLoaderException
    *    msgCode CDATA #REQUIRED
    *    exceptionClass CDATA #IMPLIED
    * >
    * <!ELEMENT Arg (#PCDATA)>
    * </code></pre>
    *
    * @param doc The document to use to create the element, may not be
    * <code>null</code>.
    *
    * @return the newly created XML element node, never <code>null</code>
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(getXmlNodeName());
      root.setAttribute(XML_ATTR_MSG_CODE, String.valueOf(m_code));
      if (m_originalExceptionClass != null)
         root.setAttribute(XML_ATTR_EXCEPTION_CLASS, m_originalExceptionClass);
      for (int i = 0; m_args != null && i < m_args.length; i++)
      {
         if (m_args[i] == null)
            PSXmlDocumentBuilder.addEmptyElement(doc, root, XML_ELEMENT_ARG);
         else
            PSXmlDocumentBuilder.addElement(doc, root, XML_ELEMENT_ARG,
               m_args[i].toString());
      }

      return root;
   }

   /**
    * Returns the localized detail message of this exception.
    *
    * @param locale The locale to generate the message in.  If <code>null
    *    </code>, the default locale is used.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getLocalizedMessage(Locale locale)
   {
      return createMessage(m_code, m_args, locale);
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getLocalizedMessage()
   {
      return getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getMessage()
   {
      return getLocalizedMessage();
   }

   /**
    * Returns a description of this exception. The format used is
    * "ExceptionClass: ExceptionMessage"
    *
    * @return the description, never <code>null</code> or empty.
    */
   public String toString()
   {
      return this.getClass().getName() + ": " + getLocalizedMessage();
   }

   /**
    * Get the parsing error code associated with this exception.
    *
    * @return The error code
    */
   public int getErrorCode()
   {
      return m_code;
   }

   /**
    * Get the parsing error arguments associated with this exception.
    *
    * @return The error arguments, may be <code>null</code>.
    */
   public Object[] getErrorArguments()
   {
      return m_args;
   }

   /**
    * Get the stack trace for the specified exception as a string.
    *
    * @param t The throwable (usually an exception), never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>t</code> is <code>null</code>.
    */
   @SuppressFBWarnings("INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE")
   public static String getStackTraceAsString(Throwable t)
   {
      if (t == null)
         throw new IllegalArgumentException("t may not be null");

      // for unknown exceptions, it's useful to log the stack trace
      StringWriter stackTrace = new StringWriter();
      PrintWriter writer = new PrintWriter(stackTrace);
      t.printStackTrace(writer);
      writer.flush();
      writer.close();

      return stackTrace.toString();
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param arrayArgs  The array of arguments to use as the arguments
    *    in the error message, may be <code>null</code> or empty.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    *    default locale is used.
    *
    * @return The formatted message, never <code>null</code>. If the appropriate
    *    message cannot be created, a message is constructed from the msgCode
    *    and args and is returned.
    *
    */
   private String createMessage(int msgCode, Object[] arrayArgs,
      Locale loc)
   {
      if (arrayArgs == null)
         arrayArgs = new Object[0];


      String msg = null;
      if (m_originalExceptionClass == null)
         msg = getErrorText(msgCode, true, loc);

      if (msg != null)
      {
         try
         {
            msg = MessageFormat.format(msg, arrayArgs);
         }
         catch (IllegalArgumentException e)
         {
            // some problem with formatting
            msg = null;
         }
      }

      if (msg == null)
      {
         String sArgs = "";
         String sep = "";

         for (int i = 0; i < arrayArgs.length; i++) {
            sArgs += sep + arrayArgs[i].toString();
            sep = "; ";
         }

         if (m_originalExceptionClass != null)
            msg = m_originalExceptionClass + ": ";
         else
            msg = "";
         msg += String.valueOf(msgCode) + ": " + sArgs;
      }

      return msg;
   }


   /**
    * Get the error text associated with the specified error code.
    *
    * @param code The error code.
    *
    * @param nullNotFound  If <code>true</code>, return <code>null</code> if the
    *    error string is not found, if <code>false</code>, return the code as
    *    a String if the error string is not found.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    * default locale is used.
    *
    * @return the error text, may be <code>null</code> or empty.
    */
   public String getErrorText(int code, boolean nullNotFound, Locale loc)
   {
      if (loc == null)
         loc = Locale.getDefault();

      ResourceBundle errorList = null;
      String errorMsg = null;
      try
      {
         errorList = getErrorStringBundle(loc);
         if (errorList != null)
         {
            errorMsg = errorList.getString(String.valueOf(code));
            return errorMsg;
         }
      }
      catch (MissingResourceException e)
      {
         // fail to get from the resource bundle, let's try it from the default
         try
         {
            errorList = getDefaultErrorStringBundle(loc);
            errorMsg = errorList.getString(String.valueOf(code));
            return errorMsg;
         }
         catch (MissingResourceException me)
         {
            // let the nullNotFound deal with this at the end.            
         }
      }

      return (nullNotFound ? null : String.valueOf(code));
   }

   /**
    * Returns a formatted string containing the test of all of the exceptions
    * contained in the supplied SQLException.
    * <p>There seems to be a bug in the Sprinta driver. We get an exception
    * for Primary key constraint violation, which has a sql warning as the
    * next exception (warning). But this next warning has a circular
    * reference to itself in the next link. So we check for this problem and
    * limit the max errors we will process to <code>20</code>.
    *
    * @param e The exception to process. If <code>null</code>, an empty
    *    string is returned.
    *
    * @return The string, never <code>null</code>, may be empty.
    */
   public static String formatSqlException(SQLException e)
   {
      if ( null == e )
         return "";

      StringBuilder errorText   = new StringBuilder();

      int errNo = 1;
      final int maxErrors = 20;
      for ( ; e != null && errNo <= maxErrors; )
      {
         errorText.append( "[" );
         errorText.append( errNo );
         errorText.append( "] " );
         errorText.append( e.getSQLState());
         errorText.append( ": " );
         errorText.append( e.getMessage());
         errorText.append( " " );
         SQLException tmp = e.getNextException();
         if ( e == tmp )
            break;
         else
            e = tmp;
         errNo++;
      }
      if ( errNo == maxErrors + 1 )
      {
         errorText.append( "[Maximum # of error messages (" );
         errorText.append( maxErrors );
         errorText.append(  ") exceeded. Rest truncated]" );
      }

      return errorText.toString();
   }

   /**
    * Gets the original exception class if one was supplied at construction.
    *
    * @return The name of the class, or <code>null</code> if one has not
    * been supplied.
    */
   public String getOriginalExceptionClass()
   {
      return m_originalExceptionClass;
   }

   /**
    * This method is used to get the string resources hash table for a
    * locale. If the resources are not already loaded for the locale,
    * they will be.
    *
    * @param loc The locale, assumed not <code>null</code>.
    *
    * @return the bundle, never <code>null</code>.
    * 
    * @throws MissingResourceException if fail to load the default resource
    *    bundle.
    */
   private ResourceBundle getErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (ms_bundle == null)
      {
         ms_bundle = ResourceBundle.getBundle(getResourceBundleBaseName(), loc);
      }

      return ms_bundle;
   }

   /**
    * Get the default resource bundle for the specified locale.
    * 
    * @param loc The locale of the resource bundle, it may be <code>null</code>.
    * 
    * @return The default resource bundle, never <code>null</code>.
    * 
    * @throws MissingResourceException if fail to load the default resource
    *    bundle.
    */
   private ResourceBundle getDefaultErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (ms_defaultBundle == null)
      {
         ms_defaultBundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_BASE, loc);
      }
      return ms_defaultBundle;
   }
   
   /**
    * Get the base name of the resource bundle, a fully qualified class name
    *
    * @return The base name of the resource bundle, never <code>null</code>
    *    or empty.
    */
   abstract protected String getResourceBundleBaseName();

   /**
    * Get the XML node name (or the root element name) for this object when
    * serialized to and from XML.
    *
    * @return The XML node name, never <code>null</code> or empty.
    */
   abstract protected String getXmlNodeName();

   /**
    * The error code of this exception, set during ctor, never modified after
    * that.
    */
   private int m_code;

   /**
    * The array of arguments to use to format the message with.  Set during
    * ctor, may be <code>null</code>, never modified after that.
    */
   private Object[] m_args;

   /**
    * If this exception was constructed from a <code>PSException</code> class,
    * this will contain the name of the class.  May be initialized during ctor,
    * otherwise <code>null</code>, never modified after that.
    */
   protected String m_originalExceptionClass = null;

   /**
    * The resource bundle containing error message formats.  <code>null</code>
    * until the first call to {@link #getErrorStringBundle(Locale)
    * getErrorStringBundle}, never <code>null</code> or modified after that
    * unless an exception occurred loading the bundle.
    */
   private static ResourceBundle ms_bundle = null;

   /**
    * The default bundle. This is initialized by getDefaultErrorStringBundle(),
    * never <code>null</code> after that. This is used when failed to find the 
    * error message from the <code>ms_bundle</code>.
    */
   private static ResourceBundle ms_defaultBundle = null;

   /**
    * The default resource bundle base.
    */
   private static final String DEFAULT_BUNDLE_BASE = 
      "com.percussion.error.PSErrorStringBundle";
      
   // xml serialization constants
   private static final String XML_ELEMENT_ARG = "Arg";
   private static final String XML_ATTR_MSG_CODE = "msgCode";
   private static final String XML_ATTR_EXCEPTION_CLASS = "exceptionClass";
}



