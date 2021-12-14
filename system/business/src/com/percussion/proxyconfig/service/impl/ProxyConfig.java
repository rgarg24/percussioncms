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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.05.16 at 05:46:39 PM ART 
//


package com.percussion.proxyconfig.service.impl;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProxyConfig", propOrder = {
    "host",
    "port",
    "user",
    "password",
    "protocols"
})
public class ProxyConfig
    extends PSAbstractDataObject
{

   @XmlElement(required = true)
   protected String host;

   @XmlElement(required = true)
   protected String port;

   @XmlElement(required = true)
   protected String user;

   @XmlElement(required = true)
   protected ProxyConfig.Password password;

   protected ProxyConfig.Protocols protocols;

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }
    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPort(String value) {
        this.port = value;
    }    

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link ProxyConfig.Password }
     *     
     */
    public ProxyConfig.Password getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProxyConfig.Password }
     *     
     */
    public void setPassword(ProxyConfig.Password value) {
        this.password = value;
    }

    
   @Override
   public Object clone()
   {
      ProxyConfig config = (ProxyConfig) super.clone();
      config.password = this.password;
      if (this.protocols != null)
      {
         config.protocols = new Protocols();
         config.protocols.setProtocols(this.protocols.getProtocols());
      }
      return config;
   }

   /**
    * Gets the value of the protocols property.
    * 
    * <p>
    * This accessor method returns a copy to the live list. Therefore any
    * modification you make to the returned list will be lost if then setter
    * method is not used to update internal list in the JAXB object.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link ProxyConfig.Protocols }
    * 
    * 
    */
   public ProxyConfig.Protocols getProtocols()
   {
      return this.protocols;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder =
   {"protocol"})
   public static class Protocols
   {

      protected List<String> protocol;

      /**
       * Gets the value of the protocol property.
       * 
       * <p>
       * This accessor method returns a copy to the live list. Therefore any
       * modification you make to the returned list will be lost if then setter
       * method is not used to update internal list in the JAXB object.
       * 
       * <p>
       * Objects of the following type(s) are allowed in the list {@link String }
       * 
       * 
       */
      public List<String> getProtocols()
      {
         if (protocol == null)
         {
            protocol = new ArrayList<>();
         }

         List<String> copy = new ArrayList<>(protocol.size());

         for (String proto : protocol)
         {
            copy.add(new String(proto));
         }
         return copy;
      }
      
      public void setProtocols(List<String> newProtocols)
      {
         protocol = new ArrayList<>();

         for (String proto : newProtocols)
         {
            protocol.add(new String(proto));
         }
      }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Password {

        @XmlValue
        protected String value;
        @XmlAttribute
        protected Boolean encrypted;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the encrypted property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isEncrypted() {
            if (encrypted == null) {
                return false;
            } else {
                return encrypted;
            }
        }

        /**
         * Sets the value of the encrypted property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setEncrypted(Boolean value) {
            this.encrypted = value;
        }
   }

}
