/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.soln.p13n.segment;

import static integrationtest.spring.SpringSetup.getBean;
import static integrationtest.spring.SpringSetup.loadXmlBeanFiles;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;

public class SegmentServiceTest {
    private static ISegmentService segmentService;
    
    
    @BeforeClass
    public static void setupSpring() throws Exception {
        loadXmlBeanFiles("file:ds/webapp/WEB-INF/applicationContext.xml",
                "file:ds/webapp/WEB-INF/spring/ds/applicationContext-ds.xml",
                "file:ds/webapp/WEB-INF/track-servlet.xml",
                "file:ds/webapp/WEB-INF/spring/ds/data-beans.xml",
                "classpath:META-INF/p13n/spring/**/*.xml",
                "classpath:integrationtest/p13n/ds/test-beans.xml");
        segmentService = getBean("segmentService", ISegmentService.class);
    }
    
    @Test
    public void testFindByPath() throws Exception {
        List<? extends Segment> segments = 
            segmentService.retrieveSegments(asList("//Folders/Segments/Regions/Western Hemisphere", 
                    "//Folders/Segments/Regions/Western Hemisphere/North America")).getList();
        assertThat(segments.get(0).getId(), is("3"));
        assertThat(segments.get(1).getId(), is("5"));
    }

}