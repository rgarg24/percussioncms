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
package com.percussion.delivery.metadata.rdbms.impl;

import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.utils.PSHashCalculator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Index;
import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

/**
 * Represents a metadata property namnatue value pair.
 * 
 * @author erikserating
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSMetadataProperty")
@Table(name = "PERC_PAGE_METADATA_PROPERTIES",indexes = {
        @Index(columnList = "ENTRY_ID", name = "entryId_hidx"),
        @Index(columnList = "NAME,DATEVALUE", name = "name_date_hidx"),
        @Index(columnList = "NAME,VALUE_HASH", name = "name_valuehash_hidx")}
)
public class PSDbMetadataProperty implements IPSMetadataProperty, Serializable
{
    @Id
    @Column(unique = true,name = "ID",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Basic
    @Nationalized
    private VALUETYPE valuetype;

    @Column(length = 4000)
    @Nationalized
    private String stringvalue;

    /**
     * Property name. For example: dcterms:creator
     */
    @Column(nullable = false, length = PSDbMetadataProperty.MAX_PROPERTY_NAME_LENGTH)
    @Nationalized
    private String name;


    @Column(length = Integer.MAX_VALUE)
    @Nationalized
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String textvalue;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date datevalue;

    @Basic
    private Double numbervalue;

    /**
     * Hash of the property's value. It's updated when calculateHash function is
     * called.
     */
    @Column(name = "VALUE_HASH", nullable = false, length = 40)
    @Nationalized
    private String valueHash;

    /**
     * This field represents the max length that the name of an instance of this
     * class can have.
     */
    public static final int MAX_PROPERTY_NAME_LENGTH = 100;


    public PSDbMetadataProperty() { }

    /**
     * HashCalculator instance used to get the hash of the metadata property's
     * value.
     */
    private static PSHashCalculator hashCalculator = new PSHashCalculator();
    /**
     * Ctor to create a property of the specified valuetype.
     * 
     * @param name the property name, cannot be <code>null</code> or empty.
     * @param type the {@link #valuetype} for the property. Cannot be
     *            <code>null</code>.
     * @param value the value to be stored in the property. May be
     *            <code>null</code> or empty.
     */
    public PSDbMetadataProperty(String name, VALUETYPE type, Object value)
    {
        this();

        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("name cannot be null or empty.");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null.");
        this.setName(name);
        boolean nan = true;
        if (type == VALUETYPE.DATE)
        {
            if (!(value instanceof Date))
                throw new IllegalArgumentException(
                        "Value type 'Date' was specified but the passed in value is not a date object.");
            setDatevalue((Date) value);
        }
        else if (type == VALUETYPE.NUMBER)
        {
            Double d = null;
            if (value instanceof Integer || value instanceof Float || value instanceof Long || value instanceof Short)
            {
                d = new Double(value.toString());
                nan = false;
            }
            else if (value instanceof Double)
            {
                d = (Double) value;
                nan = false;
            }
            else if (value instanceof String)
            {
                try
                {
                    d = Double.parseDouble(value.toString());
                    nan = false;
                }
                catch (NumberFormatException ignore)
                {

                }
            }
            if (nan)
                throw new IllegalArgumentException(
                        "The valuetype specified is 'NUMBER', but the passed in value is not a number.");
            setNumbervalue(d);
        }
        else if (type == VALUETYPE.TEXT)
        {
            String val = value.toString();
            setTextvalue(val);
        }
        else if (type == VALUETYPE.STRING)
        {
            String val = value.toString();
            if (val.length() > 4000)
                throw new IllegalArgumentException(
                        "The maximum length for a string value is 4000 chars, use a text value for greater lengths.");
            setStringvalue(val);
        }
    }

    /**
     * Convenience ctor to create a string value type property.
     * 
     * @param name cannot be <code>null</code> or empty.
     * @param value the value, may be <code>null</code>.
     */
    public PSDbMetadataProperty(String name, String value)
    {
        this(name, VALUETYPE.STRING, value);
    }

    /**
     * Convenience ctor to create a number value type property from an int
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSDbMetadataProperty(String name, int value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a double
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSDbMetadataProperty(String name, double value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a float
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSDbMetadataProperty(String name, float value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a long
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSDbMetadataProperty(String name, long value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a short
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSDbMetadataProperty(String name, short value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a Date value type property.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value may be <code>null</code>.
     */
    public PSDbMetadataProperty(String name, Date value)
    {
        this(name, VALUETYPE.DATE, value);
    }


    /**
     * @return the metadataEntry
     */
    public PSDbMetadataEntry getMetadataEntry()
    {
        return entry;
    }

    /**
     * @param metadataEntry the metadataEntry to set
     */
    public void setMetadataEntry(PSDbMetadataEntry metadataEntry)
    {
        entry = metadataEntry;
    }

    /**
     * @return the name
     */
    public String getName()
    {
       return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
       this.name=name;
    }

    @ManyToOne(optional = false)
    @JoinColumns(@JoinColumn(name = "ENTRY_ID", referencedColumnName = "pagepathhash"))
    private PSDbMetadataEntry entry;

    public String getHash()
    {
        return valueHash;

    }

     /**
     * Calculates the hash of the given value, using HashCalculator. If the
     * parameter is null, then the hash is calculated over an empty string. If
     * not, the hash is calculated over the result of 'toString' method on the
     * parameter.
     */
    public void calculateHash(Object value)
    {
        if (value == null)
            valueHash = hashCalculator.calculateHash(StringUtils.EMPTY);
        else
            valueHash = hashCalculator.calculateHash(value.toString());
    }




    /**
     * @return the valuetype
     */
    public VALUETYPE getValuetype()
    {
        return valuetype;
    }

    /**
     * @param valuetype the valuetype to set
     */
    public void setValuetype(VALUETYPE valuetype)
    {
        this.valuetype = valuetype;
    }

    /**
     * Returns the untyped value.
     * 
     * @return May be <code>null</code>.
     */
    public Object getValue()
    {
        Object result = null;
        switch (getValuetype())
        {
            case STRING :
                result = getStringvalue();
                break;

            case TEXT :
                result = getTextvalue();
                break;

            case DATE :
                result = getDatevalue();
                break;

            case NUMBER :
                result = getNumbervalue();
                break;
        }

        return result;
    }

    /**
     * @return the stringvalue
     */
    public String getStringvalue()
    {
        if (valuetype == VALUETYPE.STRING)
            return stringvalue;
        if (valuetype == VALUETYPE.TEXT)
            return textvalue;
        if (valuetype == VALUETYPE.DATE)
        {
            return datevalue.toString();
        }
        if (valuetype == VALUETYPE.NUMBER)
        {
            return numbervalue.toString();
        }
        return "";
    }

    /**
     * @param stringvalue the stringvalue to set
     */
    public void setStringvalue(String stringvalue)
    {
        this.valuetype = VALUETYPE.STRING;
        this.stringvalue = stringvalue;

        calculateHash(this.stringvalue);
    }

    /**
     * @return the textvalue
     */
    public String getTextvalue()
    {
        return textvalue;
    }

    /**
     * @param textvalue the textvalue to set
     */
    public void setTextvalue(String textvalue)
    {
        this.valuetype = VALUETYPE.TEXT;
        this.textvalue = textvalue;

        calculateHash(this.textvalue);
    }

    /**
     * @return the datevalue
     */
    public Date getDatevalue()
    {
        return Optional
                .ofNullable(datevalue)
                .map(Date::getTime)
                .map(Date::new)
                .orElse(null);
    }

    /**
     * @param datevalue the datevalue to set
     */
    public void setDatevalue(Date datevalue)
    {
        this.valuetype = VALUETYPE.DATE;
        this.datevalue = Optional
                .ofNullable(datevalue)
                .map(Date::getTime)
                .map(Date::new)
                .orElse(null);

        calculateHash(this.datevalue);
    }

    /**
     * @return the numbervalue
     */
    public Double getNumbervalue()
    {
        return numbervalue;
    }

    /**
     * @param numbervalue the numbervalue to set
     */
    public void setNumbervalue(Double numbervalue)
    {
        this.valuetype = VALUETYPE.NUMBER;
        this.numbervalue = numbervalue;

        calculateHash(this.numbervalue);
    }

    public int getId() {
        return this.id;
    }
}
