/**
 * Copyright 2005-2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krms.impl.repository

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase
import org.kuali.rice.krms.api.repository.BaseAttributeContract
import org.kuali.rice.krms.api.repository.type.KrmsAttributeDefinitionContract;

/**
 * This class contains the common elements of a KRMS attribute.
 * <p>
 * Attributes provide a way to attach custom data to an entity based on that entity's type.
 * Rules, Actions, Contexts, Agendas and Term Resolvers have their own specific
 * attribute types. This class contains their common fields. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class BaseAttributeBo extends PersistableBusinessObjectBase implements BaseAttributeContract {

	def String id
	def String attributeDefinitionId
	def String value
	def KrmsAttributeDefinitionBo attributeDefinition

   @Override
   public KrmsAttributeDefinitionContract getAttributeDefinition() {
	   return attributeDefinition
   }
} 