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

package com.percussion.delivery.comments.service.rdbms;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * 
 * @author miltonpividori
 * 
 */
@Entity
@Table(name = "PERC_COMMENT_TAGS")
public class PSCommentTag
{

    @TableGenerator(
        name="commentTagId", 
        table="PERC_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="commentTagId", 
        allocationSize=1)
    
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="commentTagId")
    private long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name="COMMENT_ID")
    private PSComment comment;
    
    @Basic
    private String name;
    
    public PSCommentTag()
    {
        
    }
    
    public PSCommentTag(String name)
    {
        this.name = name;
    }

    public long getId()
    {
        return id;
    }

    public PSComment getComment()
    {
        return comment;
    }

    public void setComment(PSComment comment)
    {
        this.comment = comment;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
