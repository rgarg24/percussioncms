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

package com.percussion.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class that represents the definition of a loadable request handler.
 */
public class PSRequestHandlerDef
{
   /**
    * Constructor for this class.
    *
    * @param handlerName The name used to identify the request handler.  
    * @param className The name of the class used to instantiate the request
    * handler.  This class must not be obfuscated.
    * @param configFile File object pointing to an existing config file for this
    * handler.  May be <code>null</code> if the handler does not have one. File
    * is not checked to see if it exists.
    * @param requestRoots Iterator over one or more String objects each
    * representing a requestRoot.  Root does not contain the server root.  Must
    * contain at least one root.
    * @throws IllegalArgumentException if handlerName, className or requestRoots
    * is <code>null</null>, or if reqeustRoots does not contain at least one
    * item.
    */
   public PSRequestHandlerDef(String handlerName, String className,
      File configFile, Iterator<String> requestRoots)
   {
      if (handlerName == null)
         throw new IllegalArgumentException("handlerName may not be null");

      if (className == null)
         throw new IllegalArgumentException("className may not be null");

      if (requestRoots == null)
         throw new IllegalArgumentException("requestRoots may not be null");

      if (!requestRoots.hasNext())
         throw new IllegalArgumentException(
            "requestRoots must contain at least one entry");

      m_handlerName = handlerName;
      m_className = className;
      m_configFile = configFile;

      m_requestRoots = new HashMap<>();
      while (requestRoots.hasNext())
      {
         String requestRoot = (String)requestRoots.next();

         // add with null request methods for now
         m_requestRoots.put(requestRoot, null);
      }
   }

   /**
    * Returns the handler name used to identify the handler.
    * @return The handler name, never <code>null</code>.
    */
   public String getHandlerName()
   {
      return m_handlerName;
   }

   /**
    * Returns the class name used to instantiate the handler.
    * @return The class name, never <code>null</code>.
    */
   public String getClassName()
   {
      return m_className;
   }

   /**
    * Returns the config file used to initialize the handler.
    * @return The config file, may be <code>null</code> if handler does not use
    * one.
    */
   public File getConfigFile()
   {
      return m_configFile;
   }

   /**
    * Returns an iterator over at least one or more request roots as Strings.
    * @return The iterator of request roots.  Must have at least one entry.
    */
   public Iterator<String> getRequestRoots()
   {
      return m_requestRoots.keySet().iterator();
   }

   /**
    * Adds request methods for the specified root.
    *
    * @param requestRoot The request root for which the methods are valid.
    * @param methods Iterator over request methods as Strings (i.e. "POST", or
    * "GET").
    * @throws IllegalArgumentException if requestRoot or methods is
    * <code>null</code>, or if the specified requestRoot is not found in the
    * list of request roots passed into the constructor.
    */
   public void addRequestMethods(String requestRoot, Iterator<String> methods)
   {
      if (requestRoot == null)
         throw new IllegalArgumentException("requestRoot may not be null");

      if (methods == null)
         throw new IllegalArgumentException("methods may not be null");

      if (!m_requestRoots.containsKey(requestRoot))
         throw new IllegalArgumentException("requestRoot not found");

      ArrayList<String> methodList = new ArrayList<>();
      while (methods.hasNext())
      {
         methodList.add(methods.next());
      }

      m_requestRoots.put(requestRoot, methodList);
   }

   /**
    * Returns the list of request methods supported by the specified root.
    * @return Iterator over one or more request methods (i.e. "POST" or "GET").
    * May be <code>null</code> if none have been set on this root.
    * @throws IllegalArgumentException if requestRoot is
    * <code>null</code>, or if the specified requestRoot is not found in the
    * list of request roots passed into the constructor.
    */
   @SuppressWarnings(value="unchecked")
   public Iterator<String> getRequestMethods(String requestRoot)
   {
      if (requestRoot == null)
         throw new IllegalArgumentException("requestRoot may not be null");

      if (!m_requestRoots.containsKey(requestRoot))
         throw new IllegalArgumentException("requestRoot not found");

      Iterator result = null;

      ArrayList<String> methodList = (ArrayList)m_requestRoots.get(requestRoot);
      if (methodList != null)
         result = methodList.iterator();

      return result;
   }

   /**
    * Name used to identify this handler.  Initialized in the
    * constructor, never <code>null</code> after that.
    */
   private String m_handlerName = null;

   /**
    * Class name used to instantiate this handler.  Initialized in the
    * constructor, never <code>null</code> after that.
    */
   private String m_className = null;

   /**
    * Config file used to initialize this handler.  Initialized in the
    * constructor, may be <code>null</code>.
    */
   private File m_configFile = null;


   /**
    * List of request roots and their request methods.  Root name is the key,
    * and an ArrayList of requestMethods is the value.  Initialized in the
    * constructor, modified in any call to
    * {@link #addRequestMethods(String, Iterator)}.
    */
   private HashMap<String,ArrayList<String>> m_requestRoots = null;
}
