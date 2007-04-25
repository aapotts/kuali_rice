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
<%@ taglib prefix="c" uri="/tlds/c.tld"%>
<%@ taglib uri="/tlds/struts-html.tld" prefix="html"%>
<%@ taglib uri="/tlds/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/tlds/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="kul"%>
<%@ taglib tagdir="/WEB-INF/tags/dd" prefix="dd"%>

<kul:tab tabTitle="Foreign Draft" defaultOpen="false" tabErrorKey="${Constants.DV_FOREIGNDRAFTS_TAB_ERRORS}">
	<c:set var="wireTransAttributes" value="${DataDictionary.DisbursementVoucherWireTransfer.attributes}" />
    <div class="tab-container" align=center>
    <div class="h2-container">
      <h2>Foreign Draft</h2>
    </div>
              <table class="datatable" summary="Foreign Draft Section" cellpadding="0">
                <tbody>
                  <tr>
                    <td><div class="floaters" >
                       <strong>
                            <c:if test="${!fullEntryMode&&!frnEntryMode}">
                              <c:if test="${KualiForm.document.dvWireTransfer.disbursementVoucherForeignCurrencyTypeCode=='C'}">
                                DV amount is stated in U.S. dollars; convert to foreign currency
                              </c:if>  
                              <c:if test="${KualiForm.document.dvWireTransfer.disbursementVoucherForeignCurrencyTypeCode=='F'}">
                                DV amount is stated in foreign currency
                              </c:if> 
                              <html:hidden property="document.dvWireTransfer.disbursementVoucherForeignCurrencyTypeCode"/> 
                            </c:if>
                           
                            <c:if test="${fullEntryMode||frnEntryMode}">
                            <html:radio property="document.dvWireTransfer.disbursementVoucherForeignCurrencyTypeCode" value="C"/>
                            DV amount is stated in U.S. dollars; convert to foreign currency </label>
                            <br/>
                            <br/>
                            <html:radio property="document.dvWireTransfer.disbursementVoucherForeignCurrencyTypeCode" value="F"/>
                            DV amount is stated in foreign currency <br/>
                           </c:if>
                           
                        <br/>
                        <kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.disbVchrCurrencyTypeName}"/>&nbsp;
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.disbVchrCurrencyTypeName}" property="document.dvWireTransfer.disbursementVoucherForeignCurrencyTypeName" readOnly="${!fullEntryMode&&!frnEntryMode}"/>
                      </strong></div></td>
                  </tr>
                </tbody>
              </table>   
        </div>
</kul:tab>
