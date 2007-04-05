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
<%@ taglib prefix="c" uri="/tlds/c.tld" %>
<%@ taglib uri="/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="kul" %>

<%@ attribute name="editingMode" required="true" description="used to decide if items may be edited" type="java.util.Map"%>
<c:set var="readOnly" value="${not empty editingMode['viewOnly']}" />

<kul:tab tabTitle="Credit Card Receipts" defaultOpen="true" tabErrorKey="${Constants.CREDIT_CARD_RECEIPTS_LINE_ERRORS}">
<c:set var="ccrAttributes" value="${DataDictionary.CreditCardDetail.attributes}" />
 <div class="tab-container" align=center>
	<div class="h2-container">
	<h2>Credit Card Receipts</h2>
	</div>
	<table cellpadding=0 class="datatable" summary="Credit Card Receipts section">
		<tr>
            <kul:htmlAttributeHeaderCell literalLabel="&nbsp;"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${ccrAttributes.financialDocumentCreditCardTypeCode}"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${ccrAttributes.financialDocumentCreditCardVendorNumber}"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${ccrAttributes.creditCardDepositDate}"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${ccrAttributes.creditCardDepositReferenceNumber}"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${ccrAttributes.creditCardAdvanceDepositAmount}"/>
            <c:if test="${not readOnly}">
                <kul:htmlAttributeHeaderCell literalLabel="Actions"/>
            </c:if>
		</tr>
        <c:if test="${not readOnly}">
            <tr>
                <kul:htmlAttributeHeaderCell literalLabel="add:" scope="row"/>
                <td class="infoline">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.financialDocumentCreditCardTypeCode}" property="newCreditCardReceipt.financialDocumentCreditCardTypeCode" />
                	&nbsp;
                	<kul:lookup boClassName="org.kuali.module.financial.bo.CreditCardType" fieldConversions="financialDocumentCreditCardTypeCode:newCreditCardReceipt.financialDocumentCreditCardTypeCode" />
                </td>
                <td class="infoline">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.financialDocumentCreditCardVendorNumber}" property="newCreditCardReceipt.financialDocumentCreditCardVendorNumber" />
                	&nbsp;
                	<kul:lookup boClassName="org.kuali.module.financial.bo.CreditCardVendor" fieldConversions="financialDocumentCreditCardTypeCode:newCreditCardReceipt.financialDocumentCreditCardTypeCode,financialDocumentCreditCardVendorNumber:newCreditCardReceipt.financialDocumentCreditCardVendorNumber" lookupParameters="newCreditCardReceipt.financialDocumentCreditCardTypeCode:financialDocumentCreditCardTypeCode" />
                </td>
                <td class="infoline">
                	<kul:dateInput attributeEntry="${ccrAttributes.creditCardDepositDate}" property="newCreditCardReceipt.creditCardDepositDate" />
                </td>
                <td class="infoline">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.creditCardDepositReferenceNumber}" property="newCreditCardReceipt.creditCardDepositReferenceNumber" />
                </td>
                <td class="infoline">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.creditCardAdvanceDepositAmount}" property="newCreditCardReceipt.creditCardAdvanceDepositAmount" styleClass="amount" />
                </td>
                <td class="infoline">
                	<div align="center">
                		<html:image property="methodToCall.addCreditCardReceipt" src="images/tinybutton-add1.gif" title="Add a Credit Card Receipt" alt="Add a Credit Card Receipt" styleClass="tinybutton"/>
                	</div>
                </td>
            </tr>
        </c:if>
        <logic:iterate id="creditCardReceipt" name="KualiForm" property="document.creditCardReceipts" indexId="ctr">
            <tr>
                <kul:htmlAttributeHeaderCell literalLabel="${ctr+1}:" scope="row">
                    <%-- Outside this th, these hidden fields would be invalid HTML. --%>
                    <html:hidden property="document.creditCardReceipt[${ctr}].documentNumber" />
                    <html:hidden property="document.creditCardReceipt[${ctr}].financialDocumentTypeCode" />
                    <html:hidden property="document.creditCardReceipt[${ctr}].financialDocumentColumnTypeCode" />
                    <html:hidden property="document.creditCardReceipt[${ctr}].financialDocumentLineNumber" />
                    <html:hidden property="document.creditCardReceipt[${ctr}].versionNumber" />
                    <html:hidden property="document.creditCardReceipt[${ctr}].objectId" />
                </kul:htmlAttributeHeaderCell>
                <td class="datacell">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.financialDocumentCreditCardTypeCode}" property="document.creditCardReceipt[${ctr}].financialDocumentCreditCardTypeCode" readOnly="${readOnly}" />
                	<c:if test="${not readOnly}">
	                	&nbsp;
    	            	<kul:lookup boClassName="org.kuali.module.financial.bo.CreditCardType" fieldConversions="financialDocumentCreditCardTypeCode:document.creditCardReceipt[${ctr}].financialDocumentCreditCardTypeCode" />
                	</c:if>
                </td>
                <td class="datacell">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.financialDocumentCreditCardVendorNumber}" property="document.creditCardReceipt[${ctr}].financialDocumentCreditCardVendorNumber" readOnly="${readOnly}" />
                	<c:if test="${not readOnly}">
	                	&nbsp;
    	            	<kul:lookup boClassName="org.kuali.module.financial.bo.CreditCardVendor" fieldConversions="financialDocumentCreditCardTypeCode:document.creditCardReceipt[${ctr}].financialDocumentCreditCardTypeCode,financialDocumentCreditCardVendorNumber:document.creditCardReceipt[${ctr}].financialDocumentCreditCardVendorNumber" lookupParameters="document.creditCardReceipt[${ctr}].financialDocumentCreditCardTypeCode:financialDocumentCreditCardTypeCode" />
    	            </c:if>
                </td>
                <td class="datacell">
                	<c:choose>
                        <c:when test="${readOnly}">
                            <kul:htmlControlAttribute attributeEntry="${ccrAttributes.creditCardDepositDate}" property="document.creditCardReceipt[${ctr}].creditCardDepositDate" readOnly="true" />
                        </c:when>
                        <c:otherwise>
                            <kul:dateInput attributeEntry="${ccrAttributes.creditCardDepositDate}" property="document.creditCardReceipt[${ctr}].creditCardDepositDate" />
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="datacell">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.creditCardDepositReferenceNumber}" property="document.creditCardReceipt[${ctr}].creditCardDepositReferenceNumber" readOnly="${readOnly}"/>
                </td>
                <td class="datacell">
                	<kul:htmlControlAttribute attributeEntry="${ccrAttributes.creditCardAdvanceDepositAmount}" property="document.creditCardReceipt[${ctr}].creditCardAdvanceDepositAmount" readOnly="${readOnly}" styleClass="amount"/>
                </td>
                <c:if test="${not readOnly}">
                    <td class="datacell">
                    	<div align="center">
                    		<html:image property="methodToCall.deleteCreditCardReceipt.line${ctr}" src="images/tinybutton-delete1.gif" title="Delete a Credit Card Receipt" alt="Delete a Credit Card Receipt" styleClass="tinybutton"/>
                    	</div>
                    </td>
                </c:if>
            </tr>
        </logic:iterate>
		<tr>
	 		<td class="total-line" colspan="5">&nbsp;</td>
	  		<td class="total-line" ><strong>Total: $${KualiForm.document.currencyFormattedTotalCreditCardAmount}</strong><html:hidden write="false" property="document.totalCreditCardAmount" /></td>
            <c:if test="${not readOnly}">
                <td class="total-line">&nbsp;</td>
            </c:if>
		</tr>
	</table>
  </div>
</kul:tab>