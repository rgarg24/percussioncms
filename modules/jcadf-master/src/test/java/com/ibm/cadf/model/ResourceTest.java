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

package com.ibm.cadf.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.ibm.cadf.exception.CADFException;

public class ResourceTest
{

    @Test
    public void testResourcePositive() throws CADFException, IOException
    {
        String initiatorId = Identifier.generateUniqueId();
        Resource initiator = new Resource(initiatorId);
        initiator.setTypeURI("/testcase");
        initiator.setName("AuditLoggerTest");
        assertEquals(true, initiator.isValid());
    }

    @Test
    public void testResourceNegative() throws CADFException, IOException
    {
        String initiatorId = Identifier.generateUniqueId();
        Resource initiator = new Resource(initiatorId);
        initiator.setTypeURI("");
        initiator.setName("AuditLoggerTest");
        assertEquals(false, initiator.isValid());
    }
}
