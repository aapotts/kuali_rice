<%--
 Copyright 2007 The Kuali Foundation.
 
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

<%@ attribute name="field" required="true" type="org.kuali.core.web.ui.Field"%>

<c:forEach items="${field.fieldValidValues}" var="radio">
    <input type="radio"
        ${field.propertyValue eq radio.key ? 'checked="checked"' : ''}
           name='${field.propertyName}'
           value='<c:out value="${radio.key}"/>'
        ${onblurcall} />
    <c:out value="${radio.label}"/>
</c:forEach>
