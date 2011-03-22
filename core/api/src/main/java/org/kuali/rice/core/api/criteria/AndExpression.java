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
package org.kuali.rice.core.api.criteria;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * An immutable composite expression which implements "and-ing" of multiple
 * expressions together. 
 * 
 * <p>Constructed as part of a {@link Criteria} when built using a
 * {@link CriteriaBuilder}.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@XmlRootElement(name = AndExpression.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = AndExpression.Constants.TYPE_NAME)
public final class AndExpression extends AbstractCompositeExpression {
	
	private static final long serialVersionUID = -6575256900578172242L;

	/**
	 * Used only by JAXB for construction.
	 */
	@SuppressWarnings("unused")
	private AndExpression() {
		super();
	}
	
	/**
	 * Construct an "And" expression from the given list of expressions.  The given list
	 * of expressions can be null or empty.  If the list is null then it will be
	 * translated internally to an empty list.
	 * 
	 * @param expressions the List of expressions to set on the And expression
	 */
	AndExpression(List<Expression> expressions) {
	    super(expressions);
	}
	
	/**
     * Defines some internal constants used on this class.
     */
    static class Constants {
        final static String ROOT_ELEMENT_NAME = "and";
        final static String TYPE_NAME = "AndType";
    }

}
