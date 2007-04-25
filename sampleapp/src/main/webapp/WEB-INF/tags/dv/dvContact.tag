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
<%@ taglib tagdir="/WEB-INF/tags" prefix="kul"%>
<%@ taglib tagdir="/WEB-INF/tags/dd" prefix="dd"%>

<kul:tab tabTitle="Contact Information" defaultOpen="true" tabErrorKey="${Constants.DV_CONTACT_TAB_ERRORS}">
  	<c:set var="dvAttributes" value="${DataDictionary.KualiDisbursementVoucherDocument.attributes}" />
    <div class="tab-container" align=center > 
    <div class="h2-container">
<h2>Contact Information</h2>
</div>



			<table class="datatable" summary="Contact Information" cellpadding="0">
                <tbody>
                  <tr>
                    <th width="25%"><div align="right"><kul:htmlAttributeLabel attributeEntry="${dvAttributes.disbVchrContactPersonName}"/></div></th>
                    <td width="25%"><kul:htmlControlAttribute attributeEntry="${dvAttributes.disbVchrContactPersonName}" property="document.disbVchrContactPersonName" readOnly="${!fullEntryMode}"/></td>
                  </tr>
                  <tr>
                    <th scope="row"><div align="right"><kul:htmlAttributeLabel attributeEntry="${dvAttributes.disbVchrContactPhoneNumber}"/></div></th>
                    <td><kul:htmlControlAttribute attributeEntry="${dvAttributes.disbVchrContactPhoneNumber}" property="document.disbVchrContactPhoneNumber" readOnly="${!fullEntryMode}"/></td>
                  </tr>
                  <tr>
                    <th scope="row"><div align="right"> <kul:htmlAttributeLabel attributeEntry="${dvAttributes.disbVchrContactEmailId}"/></div></th>
                    <td><kul:htmlControlAttribute attributeEntry="${dvAttributes.disbVchrContactEmailId}" property="document.disbVchrContactEmailId" readOnly="${!fullEntryMode}"/></td>
                  </tr>
                  <tr>
                    <th scope="row"><div align="right"><kul:htmlAttributeLabel attributeEntry="${dvAttributes.campusCode}"/></div></th>
                    <td><kul:htmlControlAttribute attributeEntry="${dvAttributes.campusCode}" property="document.campusCode" readOnly="true"/></td>
                  </tr>
                  <tr>
                    <th scope="row"><div align="right"><kul:htmlAttributeLabel attributeEntry="${dvAttributes.disbVchrCheckStubText}"/></div></th>
                    <td><kul:htmlControlAttribute attributeEntry="${dvAttributes.disbVchrCheckStubText}" property="document.disbVchrCheckStubText" readOnly="${!fullEntryMode}"/></td>
                  </tr>
                </tbody>
              </table>
    </div>
</kul:tab>
