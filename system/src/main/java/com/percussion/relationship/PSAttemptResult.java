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

package com.percussion.relationship;

/**
 * This class may define additional methods to pass sufficient informtion as
 * attempt result to relationship engine after executing the
 * {@link IPSEffect#attempt() method}. This information includes whether to test
 * for dependents' processing and error details if the test fails for some
 * reason.
 */
public class PSAttemptResult  extends PSEffectResult
{
   /**
    * Default constructor. Does not do much.
    */
   public PSAttemptResult()
   {
   }

   /**
    * Implementation for an abstract method, that does NOT allow to set a
    * recursion flag by always throwing UnsupportedOperationException.
    * This is to indicate to the effect implementer that it is an illegal
    * operation for the attempt method.
    *
    * @param recurseDependents
    * @throws UnsupportedOperationException always.
    */
   public void setRecurseDependents(boolean recurseDependents)
   {
      throw new UnsupportedOperationException("not allowed for attempt.");
   }
}