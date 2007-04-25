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
<%@ taglib prefix="logic" uri="/tlds/struts-logic.tld" %>

<%@ taglib prefix="kul" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="cr" tagdir="/WEB-INF/tags/cr" %>

<%@ attribute name="checkDetailMode" required="true" %>
<%@ attribute name="editingMode" required="true" type="java.util.Map" %>
<%@ attribute name="totalAmount" required="false" %>
<%@ attribute name="displayHidden" required="true" %>

<c:set var="checkBaseAttributes" value="${DataDictionary.CheckBase.attributes}" />

<c:set var="readOnly" value="${!empty editingMode['viewOnly']}" />
<c:set var="columnCount" value="5" />
<c:if test="${!readOnly}">
    <c:set var="columnCount" value="${columnCount + 1}" />
</c:if>

<c:if test="${checkDetailMode}">
    <kul:tab tabTitle="Check Detail" defaultOpen="true" tabErrorKey="${Constants.EDIT_CASH_RECEIPT_CHECK_DETAIL_ERRORS}" >
        <div class="tab-container" align=center>
        
	        <div class="h2-container">
			<h2>Check Detail</h2>
			</div>
            <table cellpadding=0 class="datatable" summary="check detail information">
                
                <tr>
                    <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.checkNumber}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.checkDate}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.description}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.amount}" />
                                
                    <c:if test="${!readOnly}">
                        <kul:htmlAttributeHeaderCell literalLabel="Action" />
                    </c:if>
                </tr>
    			<c:if test="${!readOnly}">
                <cr:checkLine readOnly="${readOnly}" rowHeading="add" propertyName="newCheck" actionImage="images/tinybutton-add1.gif" actionAlt="add" actionMethod="addCheck" cssClass="infoline" displayHidden="${displayHidden}" />
                </c:if>            
                <logic:iterate id="check" name="KualiForm" property="document.checks" indexId="ctr">
                    <cr:checkLine readOnly="${readOnly}" rowHeading="${ctr + 1}" propertyName="document.check[${ctr}]" baselinePropertyName="baselineCheck[${ctr}]" actionImage="images/tinybutton-delete1.gif" actionAlt="delete" actionMethod="deleteCheck.line${ctr}" cssClass="datacell" displayHidden="${displayHidden}" />
                </logic:iterate>
    
                <c:if test="${!empty totalAmount}">
                    <tr>
                        <td class="total-line" colspan="4">&nbsp;</td>
                        <td class="total-line"><strong>Total: $${totalAmount}</strong></td>
                        <c:if test="${!readOnly}">
                            <td class="total-line">&nbsp;</td>
                        </c:if>
                    </tr>
                </c:if>
            </table>
        </div>	
    </kul:tab>
</c:if>

<c:if test="${!checkDetailMode}">
    <kul:hiddenTab forceOpen="true">
        <%-- maintain state of hidden checkLines --%>
        <logic:iterate id="check" name="KualiForm" property="document.checks" indexId="ctr">
            <cr:hiddenCheckLine propertyName="document.check[${ctr}]" baselinePropertyName="baselineCheck[${ctr}]"  displayHidden="${displayHidden}" />
        </logic:iterate>
    </kul:hiddenTab>
</c:if>