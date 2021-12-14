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

package com.percussion.extensions.encoding;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;
import org.owasp.encoder.Encode;

import java.io.File;

public class PSURIComponentEncode implements IPSFieldInputTransformer{

	/***
	 * Default public constructor
	 */
	public PSURIComponentEncode(){}
	
	@Override
	public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException {
		  PSExtensionParams ep = new PSExtensionParams(params);
	      String value = ep.getStringParam(0, null, true);
		
		return Encode.forUriComponent(value);
	}

	@Override
	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
		// TODO Auto-generated method stub
		
	}

}
