/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.api.permission;

import java.util.List;

import org.kuali.rice.core.api.mo.GloballyUnique;
import org.kuali.rice.core.api.mo.Versioned;
import org.kuali.rice.core.api.mo.active.Inactivatable;

/**
 * This is the contract for a KimPermission. Represents a single permission within the system.
 * 
 * Permissions are attached to roles. All authorization checks should be done against permissions,
 * never against roles or groups.
 *  
 */
public interface PermissionContract extends Versioned, GloballyUnique, Inactivatable{

    /**
     * The unique identifier for the Permission.
     *
     * @return id
     */
    String getId();
    
    /**
     * The namespace code that this Permission belongs too.
     *
     * @return namespaceCode
     */
    String getNamespaceCode();
    
    /**
     * The name of the Permission.
     *
     * @return name
     */
    String getName();
    
    /**
     * The description of the Permission.
     *
     * @return description
     */
	String getDescription();

    /**
     * The Template ID referenced by the Permission.
     * 
     * @return templateId
     */
	String getTemplateId();   
	
   /** 
	 * List of Attributes for a Permission. 
	 * 
	 * @ return attributes
	 */
    // TODO does this need to extend something else?
    // GroupContract has List<? extends GroupAttributeContract> getAttributes();
    List<? extends PermissionAttributeContract> getAttributes();
}
