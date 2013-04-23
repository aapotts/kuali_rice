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

<%@ attribute name="channelTitle" required="true" %>
<%@ attribute name="channelUrl" required="true" %>
<%@ attribute name="selectedTab" required="true" %>

<c:choose>
  <c:when test='${!empty channelTitle && !empty channelUrl}'>
    <c:if test="${!empty param.backdoorId}">
      <c:set var="channelUrl" value="${channelUrl}?backdoorId=${param.backdoorId}&methodToCall.login=1"/>
    </c:if>
    <div id="iframe_portlet_container_div">
      <portal:iframePortletContainer channelTitle="${channelTitle}" channelUrl="${channelUrl}" />
    </div>
  </c:when>
  <c:otherwise>
  <div class="container-fluid body-container">
    <table border="0" width="100%"  cellspacing="0" cellpadding="0" id="iframe_portlet_container_table">
        <tr valign="top" bgcolor="#FFFFFF">
            <td width="15" class="leftback-focus">&nbsp;</td>
        <c:choose>
          <%-- then default to tab based actions if they are not focusing in --%>
              <c:when test='${selectedTab == "portalResearcherBody"}'>
                  <portal:portalResearcherBody />
              </c:when>

              <c:when test='${selectedTab == "portalUnitBody"}'>
                  <portal:portalUnitBody />
              </c:when>

              <c:when test='${selectedTab == "portalCentralAdminBody"}'>
                  <portal:portalCentralAdminBody />
              </c:when>
              <c:when test='${selectedTab == "portalMaintenanceBody"}'>
                  <portal:portalMaintenanceBody />
              </c:when>
              <c:when test='${selectedTab == "portalSystemAdminBody"}'>
                  <portal:portalSystemAdminBody />
              </c:when>

              <%-- as backup go to the main menu index --%>
              <c:otherwise>
                <portal:portalResearcherBody />
              </c:otherwise>
            </c:choose>
         </tr>
      </table>
    </div>
  </c:otherwise>
</c:choose>

 <div class="footerbevel">&nbsp;</div>
 <div id="footer-copyright">
 <bean:message key="app.copyright" />
 <div class="footer-build">${ConfigProperties.version} (${ConfigProperties.datasource.ojb.platform})</div>

<div class="login-form">
  <c:choose>
    <c:when test="${empty UserSession.loggedInUserPrincipalName}" >
    </c:when>
    <c:when test="${fn:trim(ConfigProperties.environment) == fn:trim(ConfigProperties.production.environment.code)}" >
      <html:form action="/logout.do" method="post" style="margin:0; display:inline">
        <input name="imageField" type="submit" value="Logout" class="go" title="Click to logout.">
      </html:form>
    </c:when>
    <c:otherwise>
      <c:set var="backboorEnabled" value="<%=org.kuali.rice.coreservice.framework.CoreFrameworkServiceLocator.getParameterService().getParameterValueAsBoolean(org.kuali.rice.kew.api.KewApiConstants.KEW_NAMESPACE, org.kuali.rice.krad.util.KRADConstants.DetailTypes.BACKDOOR_DETAIL_TYPE, org.kuali.rice.kew.api.KewApiConstants.SHOW_BACK_DOOR_LOGIN_IND)%>"/>
      <c:if test="${backboorEnabled}">
        <html:form action="/backdoorlogin.do" method="post" style="margin:0; display:inline">
          <input name="backdoorId" type="text" class="searchbox" size="10" title="Enter your backdoor ID here.">
          <button type="submit" value="Login" class="btn btn-mini" title="Click to login.">Login</button>
          <input name="methodToCall" type="hidden" value="login" />
        </html:form>
      </c:if>
      <html:form action="/backdoorlogin.do" method="post" style="margin:0; display:inline">
        <button name="imageField" type="submit" value="Logout" class="btn btn-mini">Logout</button>
        <input name="methodToCall" type="hidden" value="logout" />
      </html:form>
    </c:otherwise>
  </c:choose>
  </div>
 </div>

