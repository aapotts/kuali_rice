<%@ taglib uri="../../tld/struts-html-el.tld" prefix="html-el" %>
<%@ taglib uri="../../tld/struts-bean-el.tld" prefix="bean-el" %>
<%@ taglib uri="../../tld/struts-logic-el.tld" prefix="logic-el"%>
<%@ taglib uri="../../tld/c.tld" prefix="c" %>
<%@ taglib uri="../../tld/fmt.tld" prefix="fmt" %>
<%@ taglib uri="../../tld/displaytag.tld" prefix="display-el" %>

          <table width="100%" border=0 cellspacing=0 cellpadding=0>
					<tr>
						<td width="20%" align=right class="thnormal">Action Request Id:</td>
						<td class="datacell1"><c:out value="${actionRequest.actionRequestId}" />&nbsp;</td>
					</tr>
					<tr>
						<td align=right class="thnormal">Request Status:</td>
						<td class="datacell1"><c:out value="${actionRequest.statusLabel}" />&nbsp;</td>
					</tr>
					<tr>
						<td align=right class="thnormal">Route Node:</td>
						<td class="datacell1"><c:out value="${actionRequest.routeLevelName}" />&nbsp;</td>
					</tr>
					<tr>
						<td align=right class="thnormal">Responsibility Id:</td>
						<td class="datacell1"><c:out value="${actionRequest.responsibilityId}" />&nbsp;</td>
					</tr>
					<tr>
						<td align=right class="thnormal">Routing Priority:</td>
						<td class="datacell1"><c:out value="${actionRequest.priority}" />&nbsp;</td>
					</tr>
					<tr>
						<td align=right class="thnormal">Responsibility:</td>
						<td class="datacell1"><c:out value="${actionRequest.responsibilityDesc}" />&nbsp;</td>
					</tr>
					<tr>
						<td align=right class="thnormal">Annotation:</td>
						<td class="datacell1"><c:out value="${actionRequest.annotation}" />&nbsp;</td>
					</tr>
					<c:if test="${actionRequest.ruleBaseValuesId != null}">
					<tr>
						<td align=right class="thnormal">Rule:</td>
						<td class="datacell1">
                  <a href="
										<c:url value="Rule.do">
											<c:param name="currentRuleId" value="${actionRequest.ruleBaseValuesId}" />
											<c:param name="methodToCall" value="report" />
										</c:url>"><c:out value="${actionRequest.ruleBaseValuesId}" /></a></td>
					</tr>
					</c:if>
				<c:if test="${! empty actionRequest.childrenRequests}">
				<tr>
				  <td colspan="4">
					<table width="100%" border=0 cellspacing=0 cellpadding=0>
	                  <tr>
	                    <td class="headercell3-b-l" width="5%">&nbsp;</td>
	  		            <td width="15%" class="headercell3-b-l">Action</td>
	  		            <td width="15%" class="headercell3-b-l">Requested Of</td>
	  		            <td width="22%" class="headercell3-b-l">Time/Date</td>
	  		            <td width="40%" class="headercell3-b-l">Annotation</td>
	                  </tr>
  		              <c:set var="currentLevel" value="${level+1}" scope="page"/>
		              <c:forEach var="actionRequest" items="${actionRequest.childrenRequests}" varStatus="arStatus">
                        <c:set var="level" value="${currentLevel}" scope="request"/>
				    	<c:set var="index" value="${index}z${arStatus.index + shiftIndex}" scope="request" />
		                <c:set var="actionRequest" value="${actionRequest}" scope="request"/>
		                <c:set var="hasChildren" value="${! empty actionRequest.childrenRequests}" scope="request"/>
		                <jsp:include page="ActionRequest.jsp" flush="true" />
                      </c:forEach>
                    
                    </table>
                  </td>
                </tr>
                </c:if>
          </table>
