/**
 * Copyright 2005-2012 The Kuali Foundation
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
package org.kuali.rice.kim.framework.identity.employment

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.kuali.rice.kim.api.identity.CodedAttribute
import org.kuali.rice.kim.api.identity.CodedAttributeContract
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase

public class EntityEmploymentStatusEbo extends PersistableBusinessObjectBase implements CodedAttributeContract {
    String code;
    String name;
    boolean active;
    String sortCode;


    /**
   * Converts a mutable EmploymentStatusEbo to an immutable EmploymentStatus representation.
   * @param bo
   * @return an immutable EmploymentStatus
   */
  static CodedAttribute to(EntityEmploymentStatusEbo bo) {
    if (bo == null) { return null }
    return CodedAttribute.Builder.create(bo).build()
  }

  /**
   * Creates a EmploymentStatus business object from an immutable representation of a EmploymentStatus.
   * @param an immutable EmploymentStatus
   * @return a EmploymentStatusBo
   */
  static EntityEmploymentStatusEbo from(CodedAttribute immutable) {
    if (immutable == null) {return null}

    EntityEmploymentStatusEbo bo = new EntityEmploymentStatusEbo()
    bo.code = immutable.code
    bo.name = immutable.name
    bo.sortCode = immutable.sortCode
    bo.active = immutable.active
    bo.versionNumber = immutable.versionNumber
    bo.objectId = immutable.objectId

    return bo;
  }

  void refresh() { }
}
