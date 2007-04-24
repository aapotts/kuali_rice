/*
 * Copyright 2006 The Kuali Foundation.
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
package org.kuali.core.rule.event;

import org.kuali.core.bo.AdHocRoutePerson;
import org.kuali.core.document.Document;
import org.kuali.core.rule.AddAdHocRoutePersonRule;
import org.kuali.core.rule.BusinessRule;
import org.kuali.core.util.ObjectUtils;

/**
 * This class represents the add AdHocRoutePerson event that is part of an eDoc in Kuali. This is triggered when a user presses the
 * add button for a given adHocRoutePerson.
 * 
 * 
 */
public final class AddAdHocRoutePersonEvent extends KualiDocumentEventBase {
    private AdHocRoutePerson adHocRoutePerson;

    /**
     * Constructs an AddAdHocRoutePersonEvent with the specified errorPathPrefix, document, and adHocRoutePerson
     * 
     * @param document
     * @param adHocRoutePerson
     * @param errorPathPrefix
     */
    public AddAdHocRoutePersonEvent(String errorPathPrefix, Document document, AdHocRoutePerson adHocRoutePerson) {
        super("creating add ad hoc route person event for document " + getDocumentId(document), errorPathPrefix, document);
        this.adHocRoutePerson = (AdHocRoutePerson) ObjectUtils.deepCopy(adHocRoutePerson);
    }

    /**
     * Constructs an AddAdHocRoutePersonEvent with the given document
     * 
     * @param document
     * @param adHocRoutePerson
     */
    public AddAdHocRoutePersonEvent(Document document, AdHocRoutePerson adHocRoutePerson) {
        this("", document, adHocRoutePerson);
    }

    /**
     * This method retrieves the document adHocRoutePerson associated with this event.
     * 
     * @return AdHocRoutePerson
     */
    public AdHocRoutePerson getAdHocRoutePerson() {
        return adHocRoutePerson;
    }

    /**
     * @see org.kuali.core.rule.event.KualiDocumentEvent#validate()
     */
    public void validate() {
        super.validate();
        if (this.adHocRoutePerson == null) {
            throw new IllegalArgumentException("invalid (null) document adHocRoutePerson");
        }
    }

    /**
     * @see org.kuali.core.rule.event.KualiDocumentEvent#getRuleInterfaceClass()
     */
    public Class getRuleInterfaceClass() {
        return AddAdHocRoutePersonRule.class;
    }

    /**
     * @see org.kuali.core.rule.event.KualiDocumentEvent#invokeRuleMethod(org.kuali.core.rule.BusinessRule)
     */
    public boolean invokeRuleMethod(BusinessRule rule) {
        return ((AddAdHocRoutePersonRule) rule).processAddAdHocRoutePerson(getDocument(), this.adHocRoutePerson);
    }
}