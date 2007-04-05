<%--
 Copyright 2006 The Kuali Foundation.
 
 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl1.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ taglib uri="/tlds/struts-bean.tld" prefix="bean"%>

<%-- helpful messages --%>
<script type="text/javascript">
  function paymentMethodMessages(selectedMethod) {
    if (selectedMethod == 'W') {
		alert('<bean:message key="message.dv.feewarning"/>');
		alert('<bean:message key="message.dv.wirescreen"/>');
    }
		  
    if (selectedMethod == 'F') {
		alert('<bean:message key="message.dv.foreigndraft"/>');
    }
   }
		
   function exceptionMessage(exceptionIndicator) {
	 if (exceptionIndicator.checked == true) {
		alert('<bean:message key="message.dv.exception"/>');
     } 
   }
		
   function specialHandlingMessage(specialHandlingIndicator) {
     if (specialHandlingIndicator.checked == true) {
		alert('<bean:message key="message.dv.specialhandling"/>');
     } 
   }
		
   function documentationMessage(selectedDocumentationLocation) {
     if (selectedDocumentationLocation == 'N') {
     	// Reference error message because this error can occur via multiple paths and
     	// it didn't make sense to duplicate the error text under multiple names in ApplicationResources.properties
     	// simply for the sake of naming consistency.
		alert('<bean:message key="error.document.noDocumentationNote"/>');
     } 
     if (selectedDocumentationLocation == 'O') {
		alert('<bean:message key="message.document.initiatingOrgDocumentation"/>');
     }
    }
		
   function paymentReasonMessages(selectedPaymentReason) {
	  if (selectedPaymentReason == 'N') {
		 alert('<bean:message key="message.dv.travelnonemployee"/>');
	  } 
	  if (selectedPaymentReason == 'P') {
		 alert('<bean:message key="message.dv.travelprepaid"/>');
	  } 
    }
</script>
		