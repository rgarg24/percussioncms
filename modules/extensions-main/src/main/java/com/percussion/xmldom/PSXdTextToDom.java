/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.xmldom;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A Rhythmyx extension used to translate a text field into a temporary XML
 * DOM document.
 * <p>
 * This extension can be used either as a pre-exit or a post-exit. There are
 * six parameters:
 * <table border="1">
 *   <tr><th>Param #</th><th>Name</th><th>Description</th><th>Required?</th>
 *   <th>default value</th><tr>
 *   <tr>
 *     <td>1</td>
 *     <td>sourceFieldName</td>
 *     <td>the name of the field or file attachment. </td>
 *     <td>yes</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>XMLObjectName</td>
 *     <td>identifies the XML document where the results are stored.</td>
 *     <td>no</td>
 *     <td>XMLDOM</td>
 *   </tr>
 *   <tr>
 *     <td>3</td>
 *     <td>TidyPropertiesFile</td>
 *     <td>The tidy properties file used when parsing the source document </td>
 *     <td>no</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>4</td>
 *     <td>ServerPageTags</td>
 *     <td>The server page tags XML file used when parsing the source document </td>
 *     <td>no</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>5</td>
 *     <td>Encoding Default</td>
 *     <td>Specifies a character set encoding to use</td>
 *     <td>no</td>
 *     <td>Java's default encoding for the platform the server is running on</td>
 *   </tr>
 *   <tr>
 *     <td>6</td>
 *     <td>Validate</td>
 *     <td>"yes" indicates a validating parser should be used</td>
 *     <td>no</td>
 *     <td>no</td>
 *   </tr>
 * </table>
 * <p>
 * When used as a pre-exit, the source text field is always an HTML parameter.
 * This exit will automatically detect if the uploaded field is an attached
 * file or an HTML field, and process it accordingly. In a post-exit, the source
 * always a single XML node.
 * <p>
 * The resulting XML document is stored in a temporary object.  The default name
 * of this object is <code>XMLDOM</code>.  In a pre-exit, this object name
 * may also be <code>InputDocument</code>.  In this case, the XML document is
 * passed to the Rhythmyx server for mapping to fields.
 * <p>
 * The Tidy properties file is optional.  If it is provided, the text will be
 * run through the HTML Tidy program before parsing. See the
 * <a href="http://www.w3.org/People/Raggett/tidy" target="_blank">
 * W3C HTML Tidy</a> page
 * for details and properties file formats.
 * Note that this implementation uses the Java version of Tidy, which is not
 * exactly identical to the C implementation.
 * The pathname provided must be relative to the Rhythmyx server root.
 * <p>
 * The ServerPageTags.xml file is used to elmininate certain non-parsible
 * text generated by some editting programs.  For details see
 * {@link ProcessServerPageTags ProcessServerPageTags}. The pathname is
 * relative to the server root.
 * <p>
 * Rhythmyx provides default files for Tidy (rxW2Ktidy.properties) and
 * ServerPageTags (rxW2KServerPageTags.xml).
 *
 **/
public class PSXdTextToDom extends PSDefaultExtension
      implements IPSRequestPreProcessor, IPSResultDocumentProcessor
{

   /**
    * This method handles the pre-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdTextToDom} for parameter details.
    *
    * @param request the request context for this request
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    **/
   public void preProcessRequest(Object[] params,
                                 IPSRequestContext request)
         throws PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {
      String encodingDefault = null;
      PSXmlDomContext contxt = new PSXmlDomContext( ms_className, request );
      if (params.length < 1 || null == params[0]
            || 0 == params[0].toString().trim().length())
      {
         throw new PSParameterMismatchException( params.length, 1 );
      }
      String fileParamName = PSXmlDomUtils.getParameter( params, 0, null );

      String XMLPrivateObjectName = PSXmlDomUtils.getParameter( params, 1,
            PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT );

      setupContextParameters( contxt, params );

      if (params.length > 5 && null != params[4])
      {
         encodingDefault = params[4].toString().trim();
         if(StringUtils.isBlank(encodingDefault)){
            encodingDefault = StandardCharsets.UTF_8.name();
         }
      }

      Object inputSourceObj = request.getParameterObject( fileParamName );
      if (null == inputSourceObj)
      {
         request.printTraceMessage( "source is null, exiting" );
         return;
      }

      try
      {
         Document tempXMLDocument;
         if (inputSourceObj instanceof PSPurgableTempFile)
         {
            Charset encoding = null;
            PSPurgableTempFile inputSourceFile =
                  (PSPurgableTempFile) inputSourceObj;
            request.printTraceMessage
                  ( "Loading file " + inputSourceFile.getSourceFileName() );

            encoding = PSXmlDomUtils.determineCharacterEncoding
                  ( contxt, inputSourceFile, encodingDefault );

            tempXMLDocument = PSXmlDomUtils.loadXmlDocument
                  ( contxt, (File) inputSourceObj,encoding.name() );

         }
         else
         {
            // inputSourceObj must be a String
            tempXMLDocument = PSXmlDomUtils.loadXmlDocument
                  ( contxt, inputSourceObj.toString() );
         }

         if (XMLPrivateObjectName.equals( "InputDocument" ))
            request.setInputDocument( tempXMLDocument );
         else
            request.setPrivateObject( XMLPrivateObjectName, tempXMLDocument );
      } catch (Exception e)
      {
         contxt.handleException( e );
      }

   }


   /**
    * This method handles the post-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdTextToDom} for parameter details.
    *
    * @param request the request context for this request
    *
    * @param resultDoc the XML document resulting from the Rhythmyx server
    * operation.  The output text will be added as an XML node in this document.
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    **/
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSXmlDomContext contxt = new PSXmlDomContext( ms_className, request );
      if (params.length < 1 || null == params[0]
            || 0 == params[0].toString().trim().length())
      {
         throw new PSExtensionProcessingException( 0, "Empty or null source name" );
      }
      String textSourceName = PSXmlDomUtils.getParameter( params, 0, null );

      String XMLPrivateObjectName = PSXmlDomUtils.getParameter( params, 1,
            PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT );

      setupContextParameters( contxt, params );

      try
      {
         PSXmlTreeWalker sourceWalker = new PSXmlTreeWalker( resultDoc );

         request.printTraceMessage( "Source name is: " + textSourceName );
         String textSource =
               sourceWalker.getElementData( textSourceName, true );
         request.printTraceMessage( "Source node is: " + textSource );
         Document tempResult =
               PSXmlDomUtils.loadXmlDocument( contxt, textSource );

         request.setPrivateObject( XMLPrivateObjectName, tempResult );
      } catch (Exception e)
      {
         contxt.handleException( e );
      }

      return resultDoc;
   }


   /**
    * This exit will never modify the stylesheet
    **/
   public boolean canModifyStyleSheet()
   {
      return false;
   }


   /**
    * This method performs the common parameter checking and parsing. These
    * parameters are the same in both pre-exits and post-exits.
    **/
   private void setupContextParameters(PSXmlDomContext contxt, Object[] params)
         throws PSExtensionProcessingException
   {
      if (params.length >= 3 && null != params[2]
            && params[2].toString().trim().length() > 0)
      {
         try
         {
            contxt.setTidyProperties( params[2].toString().trim() );
         } catch (IOException e)
         {
            contxt.printTraceMessage( "Tidy Properties file " +
                  params[2].toString().trim() + " not found " );
            throw new PSExtensionProcessingException( ms_className, e );
         } catch
               (Exception e)
         {
            contxt.handleException( e );
         }
      }
      if (params.length >= 4 && null != params[3]
            && params[3].toString().trim().length() > 0)
      {
         contxt.setServerPageTags( params[3].toString().trim() );
      }
      contxt.setValidate( false );
      if (params.length >= 6 && null != params[5]
            && params[5].toString().trim().length() > 0
            && params[5].toString().toLowerCase().charAt( 0 ) == 'y')
      {
            contxt.setValidate( true );
      }
   }


   /**
    * The function name; used for error handling
    */
   private static final String ms_className = "PSXdTextToDom";
}
