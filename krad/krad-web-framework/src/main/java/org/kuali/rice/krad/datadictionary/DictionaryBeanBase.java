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
package org.kuali.rice.krad.datadictionary;

import org.kuali.rice.krad.datadictionary.parse.BeanTagAttribute;

/**
 * Common base for all objects that can be configured in the dictionary
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class DictionaryBeanBase implements DictionaryBean {

    private String namespaceCode;
    private String componentCode;

    public DictionaryBeanBase() {

    }

    /**
     * @see DictionaryBean#getNamespaceCode()
     */
    @BeanTagAttribute(name="namespaceCode")
    public String getNamespaceCode() {
        return namespaceCode;
    }

    /**
     * Setter for the bean's associated namespace code
     *
     * @param namespaceCode
     */
    public void setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
    }

    /**
     * @see DictionaryBean#getComponentCode()
     */
    @BeanTagAttribute(name="componentCode")
    public String getComponentCode() {
        return componentCode;
    }

    /**
     * Setter for the bean's associated component code
     *
     * @param componentCode
     */
    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }
}
