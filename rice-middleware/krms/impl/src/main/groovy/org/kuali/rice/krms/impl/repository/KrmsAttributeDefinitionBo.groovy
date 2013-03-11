/**
 * Copyright 2005-2013 The Kuali Foundation
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

import org.kuali.rice.core.api.mo.common.active.MutableInactivatable
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase
import org.kuali.rice.krms.api.repository.type.KrmsAttributeDefinition
import org.kuali.rice.krms.api.repository.type.KrmsAttributeDefinitionContract

public class KrmsAttributeDefinitionBo extends PersistableBusinessObjectBase implements KrmsAttributeDefinitionContract, MutableInactivatable{

	def String id
	def String name
	def String namespace
	def String label
    def String description
	def boolean active = true
	def String componentName
		
	/**
	* Converts a mutable bo to it's immutable counterpart
	* @param bo the mutable business object
	* @return the immutable object
	*/
   static KrmsAttributeDefinition to(KrmsAttributeDefinitionBo bo) {
	   if (bo == null) { return null }
	   return org.kuali.rice.krms.api.repository.type.KrmsAttributeDefinition.Builder.create(bo).build()
   }

   /**
	* Converts a immutable object to it's mutable bo counterpart
	* @param im immutable object
	* @return the mutable bo
	*/
   static KrmsAttributeDefinitionBo from(KrmsAttributeDefinition im) {
	   if (im == null) { return null }

	   KrmsAttributeDefinitionBo bo = new KrmsAttributeDefinitionBo()
	   bo.id = im.id
	   bo.name = im.name
	   bo.namespace = im.namespace
	   bo.label = im.label
       bo.description = im.description
	   bo.active = im.active
	   bo.componentName = im.componentName
	   bo.versionNumber = im.versionNumber
	   return bo
   }
 
} 