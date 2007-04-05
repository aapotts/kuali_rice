<%--
 Copyright 2006-2007 The Kuali Foundation.
 
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
<%@ taglib tagdir="/WEB-INF/tags/kra" prefix="kra" %>

<%@ attribute name="title" required="true" %>
<%@ attribute name="soft" required="true" %>
<%@ attribute name="auditType" required="true" %>

<tr><td colspan="3" class="subhead">${title}</td></tr>
<c:set var="found" value="${false}"/>
<c:forEach items="${AuditErrors}" var="cluster">
	<c:if test="${cluster.value.softAudits == soft && cluster.value.size != 0}">
		<c:if test="${!found}"><c:set var="found" value="${true}"/></c:if>
		<kra:auditRow tabTitle="${cluster.value.label}" defaultOpen="false" totalErrors="${cluster.value.size}">
			<kra:auditErrors cluster="${cluster.key}" isLink="true"/>
		</kra:auditRow>
	</c:if>
</c:forEach>
<c:if test="${!found}">
	<tr>
		<td colspan="3" height="70" align=left valign=middle class="datacell">
			<div align="center">No ${auditType} audit errors present.</div>
		</td>
	</tr>
</c:if>