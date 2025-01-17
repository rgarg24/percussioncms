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
package com.percussion.pso.imageedit.data;

import java.io.Serializable;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ImageData extends ImageMetaData implements Serializable
{
   
   private static final long serialVersionUID = -135423469L;
   private byte[] binary; 
   /**
    * 
    */
   public ImageData()
   {
      super();
   }
   
   /**
    * @return the binary
    */
   public byte[] getBinary()
   {
      return binary;
   }
   /**
    * @param binary the binary to set
    */
   public void setBinary(byte[] binary)
   {
      this.binary = binary;
   }

   @Override
   public String toString()
   {
      return super.toString();
   }
   
   
}
