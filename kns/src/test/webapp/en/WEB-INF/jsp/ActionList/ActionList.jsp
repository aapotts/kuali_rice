<%@ taglib uri="../../tld/struts-html-el.tld" prefix="html-el" %>
<%@ taglib uri="../../tld/struts-bean-el.tld" prefix="bean-el" %>
<%@ taglib uri="../../tld/struts-logic-el.tld" prefix="logic-el"%>
<%@ taglib uri="../../tld/c.tld" prefix="c" %>
<%@ taglib uri="../../tld/fmt.tld" prefix="fmt" %>
<%@ taglib uri="../../tld/displaytag.tld" prefix="display-el" %>
<html>
<head>
<title>Action List</title>

<c:if test="${! empty preferences.refreshRate && preferences.refreshRate != 0}">
<META HTTP-EQUIV="Refresh" CONTENT="<c:out value="${preferences.refreshRate * 60}"/>; URL=ActionList.do">
</c:if>

<link href="css/screen.css" rel="stylesheet" type="text/css">
<script language="JavaScript" src="scripts/en-common.js"></script>
<script language="JavaScript" src="scripts/actionlist-common.js"></script>

</head>
<body>
<html-el:form action="ActionList">
<html-el:hidden property="methodToCall" value="" />

<table width="100%" border="0" cellpadding="0" cellspacing="0" class="headercell1">
  <tr>
    <td width="10%">
        <img src="images/wf-logo.gif" alt="OneStart Workflow" width=150 height=21 hspace=5 vspace=5>&nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td align="left">
		<html-el:link page="/Preferences.do?returnMapping=viewActionList">Preferences</html-el:link>&nbsp;&nbsp;
		<a href="
			<c:url value="ActionList.do">
				<c:param name="methodToCall" value="start" />
			</c:url>">Refresh Action List</a>&nbsp;&nbsp;
		<html-el:link action="ActionListFilter">Filter</html-el:link>&nbsp;&nbsp;
		<c:if test="${UserSession.actionListFilter != null && UserSession.actionListFilter.filterOn}">
			<a href="
			<c:url value="ActionList.do">
				<c:param name="methodToCall" value="clearFilter" />
				<c:param name="key" value="${key}"/>
			</c:url>">Clear Filter</a>&nbsp;&nbsp;
		</c:if>
		<c:if test="${helpDeskActionList != null}">
			<html-el:text property="helpDeskActionListUserName" size="12"/>&nbsp;
            <html-el:image src="images/tinybutton-hlpdesk.gif" align="absmiddle" property="methodToCall.helpDeskActionListLogin" />
			<c:if test="${UserSession.helpDeskActionListUser != null}">
				<a href="
					<c:url value="ActionList.do">
						<c:param name="methodToCall" value="clearHelpDeskActionListUser" />
					</c:url>">Clear <c:out value="${UserSession.helpDeskActionListUser.displayName}"/>'s List</a>
			</c:if>&nbsp;&nbsp;
		</c:if>
		<c:if test="${! empty ActionListForm.delegators}">
            <html-el:select property="delegationId" onchange="document.forms[0].methodToCall.value='start';document.forms[0].submit();">
              <html-el:option value="${Constants.DELEGATION_DEFAULT}"><c:out value="${Constants.DELEGATION_DEFAULT}" /></html-el:option>
              <html-el:option value="${Constants.ALL_CODE}"><c:out value="${Constants.ALL_CODE}" /></html-el:option>
			  <c:forEach var="delegator" items="${ActionListForm.delegators}">
				<html-el:option value="${delegator.recipientId}"><c:out value="${delegator.displayName}" /></html-el:option>
			  </c:forEach>
            </html-el:select>
		</c:if>
    </td>
  </tr>
</table>
<table width="100%" border=0 cellspacing=0 cellpadding=0>
  <tr>
    <td width="20" height="30">&nbsp;</td>
    <td><jsp:include page="../WorkflowMessages.jsp" flush="true" /></td>
    <td width="20">&nbsp;</td>
  </tr>
  <tr>
    <td></td>
    <td>
      <table width="100%" border=0 cellspacing=0 cellpadding=0>
        <tr>
          <td>
            <strong>Action List</strong>
          </td>
          <c:if test="${UserSession.helpDeskActionListUser == null && ! empty actionList && ! empty ActionListForm.defaultActions}">
            <td align="right">
               <c:set var="defaultActions" value="${ActionListForm.defaultActions}" scope="request" />
               <html-el:select styleId='defaultAction' property="defaultActionToTake">
                 <html-el:options collection="defaultActions" labelProperty="value" property="key" filter="false"/>
               </html-el:select>&nbsp;<html-el:img src="images/tinybutton-applydflt.gif" align="absmiddle" onclick="setActions();" /><br>
            </td>
          </c:if>
        </tr>
      </table>
    </td>
    <td></td>
  </tr>
  <tr>
    <td colspan="3">&nbsp;</td>
  </tr>

  <c:if test="${ActionListForm.filterLegend != null && ActionListForm.filterLegend != ''}">
    <tr>
	  <td></td>
      <td>
        <strong><c:out value="${ActionListForm.filterLegend}"/></strong>
      </td>
	  <td></td>
    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
  </c:if>

  <tr>
	<td></td>
  	<td>

  <c:url var="actionListURI" value="ActionList.do">
    <c:param name="methodToCall" value="start"/>
    <c:param name="currentPage" value="${ActionListForm.currentPage}"/>
    <c:param name="currentSort" value="${ActionListForm.currentSort}"/>
    <c:param name="currentDir" value="${ActionListForm.currentDir}"/>
  </c:url>
  <display-el:table class="bord-r-t" style="width:100%" cellspacing="0" cellpadding="0" name="actionListPage" pagesize="${preferences.pageSize}" export="true" id="result"
          decorator="edu.iu.uis.eden.actionlist.web.ActionListDecorator" excludedParams="*" 
          requestURI="${actionListURI}">
  <display-el:setProperty name="paging.banner.placement" value="both" />
  <display-el:setProperty name="export.banner" value="" />

  <%-- Since we are using the external paging and sorting features of the display tag now, if a new sortable column is added, remember to add it to the
       ActionItemComparator in the ActionListAction as well --%>

  <display-el:column sortable="true" title="Document Id" sortProperty="routeHeaderId" class="display-column">
  	<c:choose>
      <c:when test="${UserSession.helpDeskActionListUser == null}">
		  	  <a href="<c:url value="${Constants.DOC_HANDLER_REDIRECT_PAGE}" >
		  				<c:param name="docId" value="${result.routeHeaderId}"/>
		  				<c:param name="command" value="displayActionListView" />
		  			 </c:url>" <c:if test="${ActionListForm.documentPopup == Constants.ACTION_LIST_DOCUMENT_POPUP_VALUE}"> target="_blank" </c:if> class="showvisit">
				<c:out value="${result.routeHeaderId}"/>
			  </a>
        <c:if test="${result.displayParameters != null}">
          <br>
          <a id='A<c:out value="${result.actionItemIndex}"/>'
             href="<c:url value="${Constants.DOC_HANDLER_REDIRECT_PAGE}" >
		  	<c:param name="docId" value="${result.routeHeaderId}"/>
		  	<c:param name="command" value="displayActionListInlineView" />
		  </c:url>" target='iframeAL_<c:out value="${result.actionItemIndex}"/>' onclick="rend(this, false)"><img src="images/tinybutton-show.gif" alt="show" width=45 height=15 border=0 id='F<c:out value="${result.actionItemIndex}"/>'></a><br>
        </c:if>
      </c:when>
	  <c:otherwise>
		<c:out value="${result.routeHeaderId}"/>
	  </c:otherwise>
	</c:choose>
  </display-el:column>

  <c:if test="${preferences.showDocType == Constants.PREFERENCES_YES_VAL}">
	  <display-el:column property="docLabel" sortable="true" title="Type" class="display-column" />
  </c:if>
  <c:if test="${preferences.showDocTitle == Constants.PREFERENCES_YES_VAL}">
	  <display-el:column sortProperty="docTitle" sortable="true" title="Title" class="display-column">
	  	<c:out value="${result.docTitle}" />&nbsp;
	  </display-el:column>
  </c:if>
  <c:if test="${preferences.showDocumentStatus == Constants.PREFERENCES_YES_VAL}">
	  <display-el:column property="routeHeader.docRouteStatusLabel" sortable="true" title="Route Status" class="display-column" />
  </c:if>
  <c:if test="${preferences.showActionRequested == Constants.PREFERENCES_YES_VAL}">
 	<display-el:column property="actionRequestLabel" sortable="true" title="Action Requested" class="display-column" />
  </c:if>
  <c:if test="${preferences.showInitiator == Constants.PREFERENCES_YES_VAL}">
	  <display-el:column sortable="true" title="Initiator" sortProperty="routeHeader.actionListInitiatorUser.transposedName" class="display-column" >
          <a href="<c:url value="${UrlResolver.userReportUrl}">
                     <c:param name="workflowId" value="${result.routeHeader.actionListInitiatorUser.workflowUserId.workflowId}"/>
                     <c:param name="showEdit" value="no"/>
                     <c:param name="methodToCall" value="report"/></c:url>" target="_blank">
            <c:out value="${result.routeHeader.actionListInitiatorUser.transposedName}"/></a>
 	  </display-el:column>
  </c:if>

  <c:if test="${preferences.showDelegator == Constants.PREFERENCES_YES_VAL}">
    <display-el:column sortable="true" title="Delegator" sortProperty="delegatorName" class="display-column">
    	<c:choose>
        <c:when test="${result.delegatorUser != null}">
          <a href="<c:url value="${UrlResolver.userReportUrl}">
                     <c:param name="workflowId" value="${result.delegatorUser.workflowUserId.workflowId}"/>
                     <c:param name="showEdit" value="no"/>
                     <c:param name="methodToCall" value="report"/></c:url>" target="_blank">
            <c:out value="${result.delegatorUser.transposedName}"/></a>
        </c:when>
        <c:when test="${result.delegatorWorkgroup != null}">
           <a href="<c:url value="${UrlResolver.workgroupReportUrl}">
                      <c:param name="workgroupId" value="${result.delegatorWorkgroup.workflowGroupId.groupId}"/>
                      <c:param name="methodToCall" value="report"/>
                      <c:param name="showEdit" value="no"/>
                    </c:url>" target="_blank"><c:out value="${result.delegatorWorkgroup.groupNameId.nameId}"/></a>
       </c:when>
        <c:otherwise>
        	&nbsp;
        </c:otherwise>
      </c:choose>
    </display-el:column>
  </c:if>
  <c:if test="${preferences.showDateCreated == Constants.PREFERENCES_YES_VAL}">
  	<display-el:column sortable="true" title="Date Created" sortProperty="routeHeader.createDate" class="display-column">
  		<fmt:formatDate value="${result.routeHeader.createDate}" pattern="${Constants.DEFAULT_DATE_FORMAT_PATTERN}" />&nbsp;
  	</display-el:column>
  </c:if>

  <c:if test="${preferences.showWorkgroupRequest == Constants.PREFERENCES_YES_VAL}">
  	<display-el:column sortable="true" title="Workgroup Request" sortProperty="workgroup.groupNameId.nameId" class="display-column">
  		<c:choose>
  			<c:when test="${result.workgroupId != null && result.workgroupId != 0}">
  			  <a href="<c:url value="${UrlResolver.workgroupReportUrl}">
                      <c:param name="workgroupId" value="${result.workgroup.workflowGroupId.groupId}"/>
                      <c:param name="methodToCall" value="report"/>
                      <c:param name="showEdit" value="no"/>
                    </c:url>" target="_blank"><c:out value="${result.workgroup.groupNameId.nameId}"/>
              </a>
  			</c:when>
  			<c:otherwise>
  				&nbsp;
  			</c:otherwise>
  		</c:choose>
	</display-el:column>
  </c:if>

  <c:if test="${UserSession.helpDeskActionListUser == null && ActionListForm.hasCustomActions && (ActionListForm.customActionList || (preferences.showClearFyi == Constants.PREFERENCES_YES_VAL))}">
    <display-el:column title="Actions" class="display-column">
        <c:if test="${! empty result.customActions}">
          <c:set var="customActions" value="${result.customActions}" scope="request" />
          <html-el:hidden property="actions[${result.actionItemIndex}].actionItemId" value="${result.actionItemId}" />
          <html-el:select property="actions[${result.actionItemIndex}].actionTakenCd">
            <html-el:options collection="customActions" labelProperty="value" property="key" filter="false"/>
          </html-el:select>
          <c:set var="customActionsPresent" value="true" />
        </c:if>&nbsp;
    </display-el:column>
  </c:if>

  <display-el:column title="Route Log" class="display-column">
  	<div align="center"><a href="<c:url value="RouteLog.do"><c:param name="routeHeaderId" value="${result.routeHeaderId}"/></c:url>" <c:if test="${ActionListForm.routeLogPopup == Constants.ACTION_LIST_ROUTE_LOG_POPUP_VALUE}">target="_blank"</c:if>>
	  <img alt="Route Log for Document" src="images/my_route_log.gif" />
	</a></div>
  </display-el:column>

</display-el:table>
</td>
<td></td>
</tr>

  <c:if test="${UserSession.helpDeskActionListUser == null && (! empty customActionsPresent) && (preferences.showClearFyi == Constants.PREFERENCES_YES_VAL || ActionListForm.customActionList)}">
    <tr><td colspan=3>&nbsp;</td></tr>
  	<tr>
  		<td></td>
  		<td height="0" class="thnormal-fullbord">
            <div align="center">
              <a id="takeMassActions" href="javascript: setMethodToCallAndSubmit('takeMassActions')">
           		<img src="images/buttonsmall_takeactions.gif" />
              </a>
            </div>
  		</td>
  		<td></td>
	</tr>
  </c:if>

</table>

</html-el:form>

<center>
<c:if test="${UserSession.helpDeskActionListUser != null}">
	<c:out value="${UserSession.workflowUser.displayName}"/> Viewing <c:out value="${UserSession.helpDeskActionListUser.displayName}"/>'s Action List
</c:if>
</center>
<jsp:include page="../BackdoorMessage.jsp" flush="true"/>


</body>
</html>