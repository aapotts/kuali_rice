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
package org.kuali.rice.kim.impl.identity.type

import javax.persistence.Id
import javax.persistence.Column
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.ToStringBuilder

public class EntityTypeContactInfoId {
    @Id
	@Column(name = "ENT_TYP_CD")
    def final String entityTypeCode;
	@Id
	@Column(name = "ENTITY_ID")
	def final String entityId;

    /* this ctor should never be called.  It is only present for hibernate */
    private EntityTypeContactInfoId() {
        entityTypeCode = null
        entityId = null
    }

    public EntityTypeContactInfoId(String entityId, String entityTypeCode) {
        this.entityId = entityId
        this.entityTypeCode = entityTypeCode
    }

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(obj, this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
