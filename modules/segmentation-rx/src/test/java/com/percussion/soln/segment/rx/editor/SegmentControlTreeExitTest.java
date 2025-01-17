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

package com.percussion.soln.segment.rx.editor;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static com.percussion.soln.segment.rx.editor.XMLTestHelper.*;

import org.custommonkey.xmlunit.XMLUnit;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import com.percussion.soln.segment.ISegmentNode;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.ISegmentTree;
import com.percussion.soln.segment.ISegmentTreeFactory;
import com.percussion.soln.segment.rx.editor.SegmentControlTreeExit;

@RunWith(JMock.class)
public class SegmentControlTreeExitTest {
    Mockery context = new JUnit4Mockery();
    SegmentControlTreeExit exit;
    ISegmentService segmentServiceMock;
    ISegmentTreeFactory treeFactoryMock;
    SegmentMocks segMocks = new SegmentMocks(context);
    
    @BeforeClass
    public static void setUpXML() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @Before
    public void setUp() throws Exception {
        exit = new SegmentControlTreeExit();
        segmentServiceMock = context.mock(ISegmentService.class);
        treeFactoryMock = context.mock(ISegmentTreeFactory.class);
        exit.setSegmentService(segmentServiceMock);
        exit.setSegmentTreeFactory(treeFactoryMock);
    }
    
    @Test
    public void shouldReturnTreeControlXMLOfSegmentTree() throws Exception {
        /*
         * First we expect that the tree will be retrieved.
         */
        ISegmentNode root = segMocks.makeRootSegmentStub("root");
        final ISegmentTree tree = segMocks.makeTreeStub(root);
        ISegmentNode a = segMocks.makeSegmentStub(2, "a", "//rootf/af", false);
        ISegmentNode b = segMocks.makeSegmentStub(3, "b", "//rootf/af/bf", true);
        ISegmentNode c = segMocks.makeSegmentStub(4, "c", "//rootf/af/bf/cf", true);
        segMocks.addChildren(root, a);
        segMocks.addChildren(a, b);
        segMocks.addChildren(b, c);
        segMocks.noChildren(c);
        
        context.checking(new Expectations() {{  
            one(treeFactoryMock).createSegmentTreeFromService(segmentServiceMock);
            will(returnValue(tree));
        }});
        
        Document doc = exit.processResultDocument(null, null, null);
        String expected = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            +"<tree label=\"root\">"
            +"<node id=\"2\" label=\"a\" selectable=\"no\">"
              +"<node id=\"3\" label=\"b\" selectable=\"yes\">"
                +"<node id=\"4\" label=\"c\" selectable=\"yes\"/>"
              +"</node>"
            +"</node>"
          +"</tree>";
        assertXMLEqual(expected, xmlToString(doc));
    }
}
