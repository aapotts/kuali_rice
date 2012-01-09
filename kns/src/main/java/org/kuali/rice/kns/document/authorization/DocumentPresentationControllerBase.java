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
package org.kuali.rice.kns.document.authorization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;


public class DocumentPresentationControllerBase implements DocumentPresentationController {
//    private static Log LOG = LogFactory.getLog(DocumentPresentationControllerBase.class);

	private static transient ParameterService parameterService;
  
    public boolean canInitiate(String documentTypeName) {
    	return true;
    }
    
    /**
     * 
     * @param document
     * @return boolean (true if can edit the document)
     */
    public boolean canEdit(Document document){
    	boolean canEdit = false;
    	WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isInitiated() || workflowDocument.isSaved() || workflowDocument.isEnroute() || workflowDocument.isException()) {
        	canEdit = true; 
        }
        
        return canEdit;
    }
    
    
    /**
     * 
     * @param document
     * @return boolean (true if can add notes to the document)
     */
    public boolean canAnnotate(Document document){
    	return canEdit(document);
    }
    
   
    /**
     * 
     * @param document
     * @return boolean (true if can reload the document)
     */
    public boolean canReload(Document document){
    	WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	return (canEdit(document) && !workflowDocument.isInitiated()) ;
             
    }
    
    
    /**
     * 
     * @param document
     * @return boolean (true if can close the document)
     */
    public boolean canClose(Document document){
    	return true;
    }
    
    
   
    /**
     * 
     * @param document
     * @return boolean (true if can save the document)
     */
    public boolean canSave(Document document){
    	return canEdit(document);
    }
    
  
    /**
     * 
     * @param document
     * @return boolean (true if can route the document)
     */
    public boolean canRoute(Document document){
    	boolean canRoute = false;
    	WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	if (workflowDocument.isInitiated() || workflowDocument.isSaved()){
    		 canRoute = true;
    	}
    	return canRoute;
    }
        
   
    /**
     * 
     * @param document
     * @return boolean (true if can cancel the document)
     */
    public boolean canCancel(Document document){
    	return canEdit(document);
    }
    
   
    /**
     * 
     * @param document
     * @return boolean (true if can copy the document)
     */
    public boolean canCopy(Document document){
    	 boolean canCopy = false;
    	 if(document.getAllowsCopy()){
    		 canCopy = true;
    	 }
    	 return canCopy;
    }
    
    
   
    /**
     * 
     * @param document
     * @return boolean (true if can perform route report)
     */
    public boolean canPerformRouteReport(Document document){
        return getParameterService().getParameterValueAsBoolean(KRADConstants.KNS_NAMESPACE, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, KRADConstants.SystemGroupParameterNames.DEFAULT_CAN_PERFORM_ROUTE_REPORT_IND);
    }
    
   
    /**
     * 
     * @param document
     * @return boolean (true if can do ad hoc route)
     */
    public boolean canAddAdhocRequests(Document document){
    	return true;
    }
    
   
    /**
     * This method ...
     * 
     * @param document
     * @return boolean (true if can blanket approve the document)
     */
    public boolean canBlanketApprove(Document document){
    	// check system parameter - if Y, use default workflow behavior: allow a user with the permission
    	// to perform the blanket approve action at any time
    	Boolean allowBlanketApproveNoRequest = getParameterService().getParameterValueAsBoolean(KRADConstants.KNS_NAMESPACE, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, KRADConstants.SystemGroupParameterNames.ALLOW_ENROUTE_BLANKET_APPROVE_WITHOUT_APPROVAL_REQUEST_IND);
    	if ( allowBlanketApproveNoRequest != null && allowBlanketApproveNoRequest.booleanValue() ) {
    		return canEdit(document);
    	}
    	// otherwise, limit the display of the blanket approve button to only the initiator of the document
    	// (prior to routing)
    	WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	if ( canRoute(document) && StringUtils.equals( workflowDocument.getInitiatorPrincipalId(), GlobalVariables.getUserSession().getPrincipalId() ) ) {
    		return true;
    	}
    	// or to a user with an approval action request
    	if ( workflowDocument.isApprovalRequested() ) {
    		return true;
    	}
    	
    	return false;
    }
    
    public boolean canApprove(Document document) {
    	return true;
    }

    public boolean canDisapprove(Document document) {
    	// most of the time, a person who can approve can disapprove
    	return canApprove(document);
    }
    
    public boolean canSendAdhocRequests(Document document) {
    	WorkflowDocument kualiWorkflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	return !(kualiWorkflowDocument.isInitiated() || kualiWorkflowDocument.isSaved());
    }
    
    public boolean canSendNoteFyi(Document document) {
    	return true;
    }
    
    public boolean canEditDocumentOverview(Document document){
    	WorkflowDocument kualiWorkflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	return (kualiWorkflowDocument.isInitiated() || kualiWorkflowDocument.isSaved());
    }

    public boolean canFyi(Document document) {
    	return true;
    }
    
    public boolean canAcknowledge(Document document) {
    	return true;
    }
    
    /**
     * @see DocumentPresentationController#getDocumentActions(org.kuali.rice.krad.document.Document)
     */
    public Set<String> getDocumentActions(Document document){
    	Set<String> documentActions = new HashSet<String>();
    	if (canEdit(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_EDIT);
    	}
    	
    	if(canAnnotate(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_ANNOTATE);
    	}
    	 
    	if(canClose(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_CLOSE);
    	}
    	 
    	if(canSave(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_SAVE);
    	}
    	if(canRoute(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_ROUTE);
    	}
    	 
    	if(canCancel(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_CANCEL);
    	}
    	 
    	if(canReload(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_RELOAD);
    	}
    	if(canCopy(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_COPY);
    	}
    	if(canPerformRouteReport(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_PERFORM_ROUTE_REPORT);
    	}
    	
    	if(canAddAdhocRequests(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_ADD_ADHOC_REQUESTS);
    	}
    	
    	if(canBlanketApprove(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_BLANKET_APPROVE);
    	}
    	if (canApprove(document)) {
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_APPROVE);
    	}
    	if (canDisapprove(document)) {
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_DISAPPROVE);
    	}
    	if (canSendAdhocRequests(document)) {
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_SEND_ADHOC_REQUESTS);
    	}
    	if(canSendNoteFyi(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_SEND_NOTE_FYI);
    	}
    	if(this.canEditDocumentOverview(document)){
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_EDIT_DOCUMENT_OVERVIEW);
    	}
    	if (canFyi(document)) {
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_FYI);
    	}
    	if (canAcknowledge(document)) {
    		documentActions.add(KRADConstants.KUALI_ACTION_CAN_ACKNOWLEDGE);
    	}
    	return documentActions;
    }

	protected ParameterService getParameterService() {
		if ( parameterService == null ) {
			parameterService = CoreFrameworkServiceLocator.getParameterService();
		}
		return parameterService;
	}


}
