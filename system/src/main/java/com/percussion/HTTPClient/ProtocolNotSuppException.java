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

package com.percussion.HTTPClient;

import java.io.IOException;

/**
 * Signals that the protocol is not supported.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
public class ProtocolNotSuppException extends IOException
{

    /**
     * Constructs an ProtocolNotSuppException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public ProtocolNotSuppException()
    {
	super();
    }


    /**
     * Constructs an ProtocolNotSuppException class with the specified
     * detail message.  A detail message is a String that describes this
     * particular exception.
     * @param s the String containing a detail message
     */
    public ProtocolNotSuppException(String s)
    {
	super(s);
    }

}