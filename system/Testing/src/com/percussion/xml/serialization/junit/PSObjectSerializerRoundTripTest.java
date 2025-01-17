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
package com.percussion.xml.serialization.junit;

import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.IPSAssemblyTemplate.AAType;
import com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.serialization.PSObjectSerializer;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Test round tripping of specific objects with the serializer to test
 * conversion code.
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSObjectSerializerRoundTripTest
{
   /**
    * Round trip an assembly template. Use a template that has every field set
    * to a value to make sure the entire object is correctly serialized.
    * 
    * @throws Exception
    */
   @Test
   public void testRoundTripTemplate() throws Exception
   {
      PSAssemblyTemplate template = setupTemplate();

      PSObjectSerializer ser = PSObjectSerializer.getInstance();
      PSXmlSerializationHelper.addType("assembly-template",
            PSAssemblyTemplate.class);
      String str = ser.toXmlString(template);
      PSAssemblyTemplate restore = (PSAssemblyTemplate) ser.fromXmlString(str);

      assertEquals(template, restore);
   }

   /**
    * Create a template for the test
    * 
    * @return
    */
   private PSAssemblyTemplate setupTemplate() throws Exception
   {
      PSAssemblyTemplate template = new PSAssemblyTemplate();
      template.setName("test_template_0");
      template.setLabel("test template 0");
      template.setDescription("desc for tt0");
      template.setActiveAssemblyType(AAType.NonHtml);
      template.setAssembler("invalid assembler 0");
      template.setAssemblyUrl("../assembler/random_0");
      template.setCharset("invalid_charset");
      template.setGlobalTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 1101));
      template.setGlobalTemplateUsage(GlobalTemplateUsage.Defined);
      template.setGUID(new PSGuid(PSTypeEnum.TEMPLATE, 1102));
      template.setLocationPrefix("foo_");
      template.setLocationSuffix("_bar");
      template.setOutputFormat(OutputFormat.Page);
      template.setPublishWhen(PublishWhen.Never);
      template.setStyleSheetPath("some invalid stylesheet path");
      template.addBinding(new PSTemplateBinding(1, "a", "1+2"));
      template.addBinding(new PSTemplateBinding(2, "b", "2*2"));

      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      template.addSlot(asm.loadSlot("501"));
      template.addSlot(asm.loadSlot("502"));

      return template;
   }
}
