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
<%@ taglib tagdir="/WEB-INF/tags" prefix="kul"%>
<%@ taglib tagdir="/WEB-INF/tags/cm" prefix="cm" %>
<%@ taglib tagdir="/WEB-INF/tags/dd" prefix="dd" %>

<c:set var="cmDocAttributes" value="${DataDictionary.KualiCashManagementDocument.attributes}" />
<c:set var="drawerAttributes" value="${DataDictionary.KualiCashManagementDocument.attributes}" />
<c:set var="dummyAttributes" value="${DataDictionary.DummyBusinessObject.attributes}" />

<c:set var="allowOpen" value="${(KualiForm.editingMode[AuthorizationConstants.EditMode.FULL_ENTRY]) && (KualiForm.document.rawCashDrawerStatus == Constants.CashDrawerConstants.STATUS_CLOSED)}" />
<c:set var="allowRefresh" value="${KualiForm.document.rawCashDrawerStatus != Constants.CashDrawerConstants.STATUS_CLOSED}" />


<style>
    td {white-space: nowrap}
    th {white-space: nowrap}
</style>

<c:set var="subheading" value="Cash Drawer Activity" />
<c:if test="${!empty KualiForm.cashDrawerSummary}">
    <c:set var="subheading" value="${subheading} as of ${KualiForm.cashDrawerSummary.timeRefreshed}" />
</c:if>

<html:hidden property="cashDrawerSummary.timeRefreshed" />
<html:hidden property="cashDrawerSummary.timeOpened" />
<kul:tab tabTitle="Cash Drawer Activity" defaultOpen="true" tabErrorKey="${Constants.CashManagementConstants.CASH_MANAGEMENT_ERRORS}" >
    <div class="tab-container" align=center>
        <div class="h2-container">
            <h2>${subheading}</h2>
        </div>

        <table cellspacing=0 cellpadding=0 border=0 class="datatable">
            <tr>
                <td class="infoline" colspan=3>
                    <kul:htmlAttributeLabel labelFor="document.workgroupName" attributeEntry="${cmDocAttributes.workgroupName}" readOnly="true" skipHelpUrl="true" noColon="true" />
                </td>
                <td colspan=2>
                    <kul:htmlControlAttribute property="document.workgroupName" attributeEntry="${cmDocAttributes.workgroupName}" readOnly="true" />
                </td>
            </tr>

            <tr>
                <td class="infoline" colspan=3>
                    <kul:htmlAttributeLabel labelFor="document.cashDrawerStatus" attributeEntry="${cmDocAttributes.cashDrawerStatus}" readOnly="true" skipHelpUrl="true" noColon="true" />
                </td>
                <td colspan=2>
                    <kul:htmlControlAttribute property="document.cashDrawerStatus" attributeEntry="${cmDocAttributes.cashDrawerStatus}" readOnly="true"/>
                    <c:if test="${KualiForm.document.rawCashDrawerStatus == Constants.CashDrawerConstants.STATUS_OPEN}">
                        (opened at ${KualiForm.cashDrawerSummary.timeOpened})
                    </c:if>
                </td>
            </tr>

            <c:if test="${KualiForm.document.rawCashDrawerStatus != Constants.CashDrawerConstants.STATUS_CLOSED}">
                <tr>
                    <td colspan=5 class="tab-subhead">Cash Drawer Activity: <html:hidden property="cashDrawerSummary.overallStats.receiptCount" write="true" /> available Cash Receipts</td>
                </tr>
                
                <tr>
                    <td rowspan=4 >&nbsp;&nbsp;</td>
                    <td colspan=2 class="infoline">Checks</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.overallStats.checkTotal" write="true" /></td>
                    <td rowspan=4 width=100%>&nbsp;</td>
                </tr>
                <tr>
                    <td colspan=2 class="infoline">Currency</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.overallStats.currencyTotal" write="true" /></td>
                </tr>
                <tr>
                    <td colspan=2 class="infoline">Coin</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.overallStats.coinTotal" write="true" /></td>
                </tr>
                <tr>
                    <th colspan=2 style="text-align: left; padding-left: 0px">TOTAL</th>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.overallSumTotal" write="true" /></td>
                </tr>
    
                <tr>
                    <td colspan=5 class="tab-subhead">Deposit Activity: <html:hidden property="cashDrawerSummary.depositedReceiptCount" write="true" /> Deposited Cash Receipts</td>
                </tr>
                <tr>
                    <td rowspan=7>&nbsp;&nbsp;</td>
                    <td colspan=2 class="infoline">Operating</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.overallSumTotal" write="true" /></td>
                    <td rowspan=7 width=100%>&nbsp;</td>
                </tr>
                <tr>
                    <td colspan=2 class="infoline">- Interim</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.interimSumTotal" write="true" /></td>
                </tr>
                <tr>
                    <td colspan=2 class="infoline">- Final</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.finalSumTotal" write="true" /></td>
                </tr>
                <tr>
                    <th colspan=2 style="text-align: left; padding-left: 0px">= Remaining</th>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.remainingSumTotal" write="true" /></td>
                </tr>
    
                <tr>
                    <td rowspan=3>&nbsp;&nbsp;</td>
                    <td class="infoline">Checks</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.remainingCheckTotal" write="true" /></td>
                </tr>
                <tr>
                    <td class="infoline">Currency</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.remainingCurrencyTotal" write="true" /></td>
                </tr>
                <tr>
                    <td class="infoline">Coin</td>
                    <td style="text-align: right">$<html:hidden property="cashDrawerSummary.remainingCoinTotal" write="true" /></td>
                </tr>
            </c:if>
        </table>

        <div style="padding: 10px">
            <c:if test="${allowOpen}">
                <html:image src="images/buttonsmall_openCashDrawer.gif" style="border: none" property="methodToCall.openCashDrawer" title="Open Cash Drawer" alt="Open Cash Drawer" />
            </c:if>
            
            <c:if test="${allowRefresh}">
                <html:image src="images/buttonsmall_refresh.gif" style="border: none" property="methodToCall.refreshSummary" title="Refresh Cash Drawer Summary" alt="Refresh Cash Drawer Summary" />
            </c:if>
        </div>
    </div>
</kul:tab>
