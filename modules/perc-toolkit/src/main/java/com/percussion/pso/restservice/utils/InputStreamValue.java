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
package com.percussion.pso.restservice.utils;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFieldValue;
import com.percussion.util.PSPurgableTempFile;

import java.io.InputStream;
import java.util.Arrays;
//Work in progress
public class InputStreamValue extends PSFieldValue {

	 private InputStream is;
	 private PSPurgableTempFile ptf;

	 public InputStreamValue(InputStream is)
	 {
	      this.is = is;
	 }

	
	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValueAsString() throws PSCmsException {
		// TODO Auto-generated method stub
		return null;
	}

	  // see IPSFieldValue#clone() interface for description
	   public Object clone()
	   {
	      PSFieldValue copy = null;
	      copy = (PSFieldValue) super.clone();
	      return copy;
	   }

	   /**
	    * Convenience method to build the hash of the object, just checks for
	    * <code>null</code> and if objecttoHash is, it ignores and returns 0.
	    *
	    * @param objectToHash may be <code>null</code>.
	    * @return the hashCode of objectToHash
	    */
	   protected int hashBuilder(Object objectToHash)
	   {
	      int theHash = 0;
	      if(objectToHash != null)
	         theHash = objectToHash.hashCode();

	      return theHash;
	   }



	   /**
	    * Compares objects that implement the <code>equals()</code> method.
	    * <code>String</code>s will be compared with case ignored.
	    * Are they equal?
	    *
	    * @return <code>true</code>if they are, otherwise <code>false</code>.
	    */
	   protected boolean compare(Object a, Object b)
	   {
	        if(a == null || b == null)
	        {
	            if(a != null || b != null)
	                return false;
	        } else
	        {
	            if(a.getClass().isArray() && b.getClass().isArray())
	                return Arrays.equals((Object[])a, (Object[])b);
	            if(a instanceof String && b instanceof String)
	               return ((String)a).equalsIgnoreCase((String)b);
	            if(!a.equals(b))
	                return false;
	        }
	        return true;
	    }


	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}
}
