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
package org.kuali.rice.krad;

import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.WorkflowDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for working with the UserSession.
 */
public final class UserSessionUtils {

    private UserSessionUtils() {
        throw new IllegalStateException("this class should not be instantiated");
    }

    /**
     * Adds the given {@link org.kuali.rice.kew.api.WorkflowDocument} to the {@link org.kuali.rice.krad.UserSession}.
     * @param userSession the session to add the workflow document to
     * @param workflowDocument the workflow doc to add to the session
     */
    public static void addWorkflowDocument(UserSession userSession, WorkflowDocument workflowDocument) {
        @SuppressWarnings("unchecked") Map<String, WorkflowDocument> workflowDocMap =
                (Map<String, WorkflowDocument>) userSession
                        .retrieveObject(KewApiConstants.WORKFLOW_DOCUMENT_MAP_ATTR_NAME);

        if (workflowDocMap == null) {
            workflowDocMap = new HashMap<String, WorkflowDocument>();
        }

        workflowDocMap.put(workflowDocument.getDocumentId(), workflowDocument);
        userSession.addObject(KewApiConstants.WORKFLOW_DOCUMENT_MAP_ATTR_NAME, workflowDocMap);
    }

    /**
     * Returns the {@link org.kuali.rice.kew.api.WorkflowDocument} with the given ID from the
     * {@link org.kuali.rice.krad.UserSession}.  If there is not one cached in the session with
     * that ID, then null is returned.
     * @param userSession the user session from which to retrieve the workflow document
     * @param workflowDocumentId the ID of the workflow document to get
     * @return the cached workflow document, or null if a document with that ID is not cached in the user session
     */
    public static WorkflowDocument getWorkflowDocument(UserSession userSession, String workflowDocumentId) {
        @SuppressWarnings("unchecked") Map<String, WorkflowDocument> workflowDocMap =
                (Map<String, WorkflowDocument>) userSession
                        .retrieveObject(KewApiConstants.WORKFLOW_DOCUMENT_MAP_ATTR_NAME);

        if (workflowDocMap == null) {
            workflowDocMap = new HashMap<String, WorkflowDocument>();
            userSession.addObject(KewApiConstants.WORKFLOW_DOCUMENT_MAP_ATTR_NAME, workflowDocMap);
            return null;
        }

        return workflowDocMap.get(workflowDocumentId);
    }
}
