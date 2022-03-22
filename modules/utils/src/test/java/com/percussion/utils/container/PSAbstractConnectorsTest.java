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

package com.percussion.utils.container;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PSAbstractConnectorsTest {
    Map<String,String> loadMap;

    @Test
    @Before
    public void loadProperties() {
        File root = new File(getClass().getClassLoader().getResource("com/percussion/utils/container/sample.properties").getFile());
        Path rootPath = Paths.get(root.toURI());

        PSAbstractConnectors loadProp=new PSAbstractConnectors();
        loadMap = loadProp.loadProperties(rootPath);
    }

    @Test
    @After
    public void saveProperties() {
        Path root = Paths.get(new File(getClass().getClassLoader().getResource("com/percussion/utils/container/sample1.properties").getFile()).toURI());
        PSAbstractConnectors saveProp=new PSAbstractConnectors();

        saveProp.saveProperties(loadMap,root);


    }


}