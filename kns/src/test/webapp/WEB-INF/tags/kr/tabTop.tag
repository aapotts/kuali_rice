<%--
 Copyright 2005-2006 The Kuali Foundation.
 
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
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>
<%@ attribute name="tabTitle" required="true" %>
<%@ attribute name="defaultOpen" required="true" %>
<%@ attribute name="tabErrorKey" required="false" %>

<c:set var="currentTabIndex" value="${KualiForm.currentTabIndex}"/>
<c:set var="currentTab" value="${KualiForm.tabStateJstl}"/>
<c:choose>
    <c:when test="${empty currentTab}">
        <c:set var="isOpen" value="${defaultOpen}" />
    </c:when>
    <c:when test="${!empty currentTab}" >
        <c:set var="isOpen" value="${currentTab.open}" />
    </c:when>
</c:choose>
<!-- if the section has errors, override and set isOpen to true -->
<c:if test="${!empty tabErrorKey}">
  <kul:checkErrors keyMatch="${tabErrorKey}" auditMatch="${tabAuditKey}"/>
  <c:set var="isOpen" value="${hasErrors ? true : isOpen}"/>
</c:if>
<html:hidden property="tabState[${currentTabIndex}].open" value="${isOpen}" />
<!-- TAB -->

<div id="workarea">

<table width="100%" class="tab" cellpadding=0 cellspacing=0 summary="">
	<tr>
		<td class="tabtable1-left">
		    <img src="${ConfigProperties.kr.externalizable.images.url}tab-topleft.gif" alt=""	width=12 height=29 align=middle>${tabTitle}
		</td>
		<td class="tabtable1-mid">
            <c:if test="${isOpen == 'true' || isOpen == 'TRUE'}">
			    <html:image property="methodToCall.toggleTab.tab${currentTabIndex}"	src="${ConfigProperties.kr.externalizable.images.url}tinybutton-hide.gif" title="hide" alt="hide" styleClass="tinybutton" styleId="tab-${currentTabIndex}-imageToggle" onclick="javascript: return toggleTab(document, ${currentTabIndex}); " />
		    </c:if>
		    <c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
			    <html:image property="methodToCall.toggleTab.tab${currentTabIndex}"	src="${ConfigProperties.kr.externalizable.images.url}tinybutton-show.gif" title="open" alt="open" styleClass="tinybutton" styleId="tab-${currentTabIndex}-imageToggle" onclick="javascript: return toggleTab(document, ${currentTabIndex}); " />
		    </c:if>
		</td>
		<td class="tabtable1-right">
		    <img src="${ConfigProperties.kr.externalizable.images.url}tab-topright.gif" alt="" width="12" height="29" align="middle">
		</td>
	</tr>
</table>



<c:if test="${isOpen == 'true' || isOpen == 'TRUE'}"><div style="display: block;" id="tab-${currentTabIndex}-div"></c:if>
<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}"><div style="display: none;" id="tab-${currentTabIndex}-div"></c:if>
 
        <!-- display errors for this tab -->
        <div class="tab-container-error"><div class="left-errmsg-tab"><kul:errors keyMatch="${tabErrorKey}" errorTitle="Errors Found in Document:" /></div></div>
        <!-- Before the jsp:doBody of the kul:tab tag -->            
        <jsp:doBody/>            
        <!-- After the jsp:doBody of the kul:tab tag -->
      
</div>