/*
 * Copyright 2011 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.rice.kns.uif.authorization;

import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.KNSServiceLocatorWeb;
import org.kuali.rice.kns.service.MaintenanceDocumentDictionaryService;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class MaintenanceDocumentPresentationControllerBase extends DocumentPresentationControllerBase {

    protected static MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService;

    public boolean canCreate(Class<?> dataObjectClass) {
        return getMaintenanceDocumentDictionaryService().getAllowsNewOrCopy(
                getMaintenanceDocumentDictionaryService().getDocumentTypeName(dataObjectClass)).booleanValue();
    }

    @Override
    protected boolean canSave(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        return (!workflowDocument.stateIsEnroute() && super.canSave(document));
    }

    @Override
    protected boolean canBlanketApprove(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        return (!workflowDocument.stateIsEnroute() && super.canBlanketApprove(document));
    }

    protected MaintenanceDocumentDictionaryService getMaintenanceDocumentDictionaryService() {
        if (maintenanceDocumentDictionaryService == null) {
            maintenanceDocumentDictionaryService = KNSServiceLocatorWeb.getMaintenanceDocumentDictionaryService();
        }
        return maintenanceDocumentDictionaryService;
    }

}
