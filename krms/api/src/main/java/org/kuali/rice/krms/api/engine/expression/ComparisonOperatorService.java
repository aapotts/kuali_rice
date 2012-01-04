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
package org.kuali.rice.krms.api.engine.expression;

import org.kuali.rice.krms.framework.engine.expression.EngineComparatorExtension;
import org.kuali.rice.krms.framework.engine.expression.StringCoercionExtension;

import java.util.List;

/**
 * Service for registering {@link EngineComparatorExtension} for use as a {@link org.kuali.rice.krms.framework.engine.expression.ComparisonOperator}
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface ComparisonOperatorService extends StringCoercionExtension {

    /**
     * The {@link List} of {@link EngineComparatorExtension}s.
     * @return List<EngineComparatorExtension> of configured {@link EngineComparatorExtension}s.
     */
    public List<EngineComparatorExtension> getOperators();

    /**
     * List<EngineComparatorExtension> to use.
     * @param operators
     */
    public void setOperators(List<EngineComparatorExtension> operators);

    /**
     * Does the service have an Extension that can compare the given objects?
     *
     * @param leftHandSide left hand side Object
     * @param rightHandSide right hand side Object
     * @return boolean true a configured {@link EngineComparatorExtension} can compare the lhs and rhs Objects.
     */
    public boolean canCompare(Object leftHandSide, Object rightHandSide);

    /**
     * {@link EngineComparatorExtension} that canCompare the given Objects
     * @param leftHandSide left hand side Object
     * @param rightHandSide right hand side Object
     * @return the EngineComparatorExtension that can compare the given Objects.
     */
    public EngineComparatorExtension findComparatorExtension(Object leftHandSide, Object rightHandSide);

    /**
     *
     * @return List<StringCoercionExtension>
     */
    // this would move to a StringCoercionExtensionService if we go that way
    public List<StringCoercionExtension> getStringCoercionExtensions();

    /**
     * The {@link List} of {@link StringCoercionExtension}s.
     * @param stringCoercionExtensions
     */
    // this would move to a StringCoercionExtensionService if we go that way
    public void setStringCoercionExtensions(List<StringCoercionExtension> stringCoercionExtensions);

    /**
     *
     * @param type to coerce
     * @param value to coerce
     * @return {@link StringCoercionExtension} that can coerce the given type and value
     */
    // this would move to a StringCoercionExtensionService if we go that way
    StringCoercionExtension findStringCoercionExtension(String type, String value);
}
