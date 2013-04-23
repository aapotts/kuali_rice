<%--
 Copyright 2005-2009 The Kuali Foundation

 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.opensource.org/licenses/ecl2.php

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/WEB-INF/jsp/kraTldHeader.jsp"%>

<%@ attribute name="selectedTab" required="true"%>
<%@ attribute name="channelTitle" required="false" %>

<div class="navbar main-navbar navbar-static-top">
  <div class="navbar-inner">
    <div class="container-fluid">
      <ul class="nav">
        <a class="brand" href="${ConfigProperties.application.url}/portal.do">Kuali Coeus</a>
        <%-- Researcher Menu --%>
        <c:if test='${selectedTab == "portalResearcherBody"}'>
          <li class="active"><a href="portal.do?selectedTab=portalResearcherBody" title="Researcher">Researcher</a></li>
        </c:if>
        <c:if test='${selectedTab != "portalResearcherBody" && channelTitle != "Document Search" && channelTitle != "Action List"}'>
          <c:if test="${empty selectedTab}">
            <li class="active"><a href="portal.do?selectedTab=portalResearcherBody" title="Researcher">Researcher</a></li>
          </c:if>
          <c:if test="${!empty selectedTab}">
            <li><a href="portal.do?selectedTab=portalResearcherBody" title="Researcher">Researcher</a></li>
          </c:if>
        </c:if>

        <%-- Unit --%>
        <c:if test='${selectedTab == "portalUnitBody"}'>
          <li class="active"><a href="portal.do?selectedTab=portalUnitBody" title="Unit">Unit</a></li>
        </c:if>
        <c:if test='${selectedTab != "portalUnitBody"}'>
          <li><a href="portal.do?selectedTab=portalUnitBody" title="Unit">Unit</a></li>
        </c:if>

        <%-- Central Admin --%>
        <c:if test='${selectedTab == "portalCentralAdminBody"}'>
          <li class="active"><a href="portal.do?selectedTab=portalCentralAdminBody" title="Central Admin">Central Admin</a></li>
        </c:if>
        <c:if test='${selectedTab != "portalCentralAdminBody"}'>
          <li><a href="portal.do?selectedTab=portalCentralAdminBody" title="Central Admin">Central Admin</a></li>
        </c:if>

        <%-- Maintenance --%>
        <c:if test='${selectedTab == "portalMaintenanceBody"}'>
          <li class="active"><a href="portal.do?selectedTab=portalMaintenanceBody" title="Maintenance">Maintenance</a></li>
        </c:if>
        <c:if test='${selectedTab != "portalMaintenanceBody"}'>
          <li><a href="portal.do?selectedTab=portalMaintenanceBody" title="Maintenance">Maintenance</a></li>
        </c:if>

        <%-- System Admin --%>
        <c:if test='${selectedTab == "portalSystemAdminBody"}'>
          <li class="active"><a href="portal.do?selectedTab=portalSystemAdminBody" title="System Admin">System Admin</a></li>
        </c:if>
        <c:if test='${selectedTab != "portalSystemAdminBody"}'>
          <li><a href="portal.do?selectedTab=portalSystemAdminBody" title="System Admin">System Admin</a></li>
        </c:if>

        <li class="feedback right-nav">
          <a class="portal_link" href="<bean:message key="app.feedback.link"/>" target="_blank" title="<bean:message key="app.feedback.linkText" />"><bean:message key="app.feedback.linkText" /></a>
        </li>
      </ul>
    </div>
  </div>
</div>

<div class="navbar subnavbar">
  <div class="navbar-inner">
    <div class="container-fluid">
      <ul class="nav">
        <li class="first user right-nav">
          <c:set var="invalidUserMsg" value="Invalid username"/>
          <c:choose>
            <c:when test="${empty UserSession.loggedInUserPrincipalName}" >You are not logged in.</c:when>
            <c:otherwise>User: ${UserSession.loggedInUserPrincipalName}
              <c:if test="${UserSession.backdoorInUse}" >
                  Impersonating User:${UserSession.principalName}
              </c:if>
              <c:if test="${param.invalidUser}">
                  Impersonating User:&nbsp;${invalidUserMsg}
              </c:if>
            </c:otherwise>
          </c:choose>
        </li>

        <li class="right-nav">
          <portal:portalLink displayTitle="false" title='Document Search' url='${ConfigProperties.workflow.documentsearch.base.url}'>
            Doc Search
          </portal:portalLink>
        </li>

        <li class="last right-nav">
          <portal:portalLink displayTitle="false" title='Action List' url='${ConfigProperties.kew.url}/ActionList.do'>
            Action List
          </portal:portalLink>
        </li>
      </ul>
    </div>
  </div>
</div>
