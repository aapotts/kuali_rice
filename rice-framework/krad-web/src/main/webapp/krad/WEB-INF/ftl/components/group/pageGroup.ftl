<#--

    Copyright 2005-2013 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<#macro uif_pageGroup group>

<#--    Breadcrumb update-->
<div id="Uif-BreadcrumbUpdate" style="display:none;">
    <@krad.template component=KualiForm.view.breadcrumbs page=group/>
</div>

<#--unified view header supportTitle update-->
    <#if group.header?has_content && KualiForm.view.unifiedHeader>
    <div id="Uif-SupportTitleUpdate" style="display:none;">
            <span class="uif-supportTitle-wrapper uif-viewHeader-supportTitle">
                <#-- rich message support -->
                <#if group.header.richHeaderMessage?has_content>
                    <@krad.template component=group.richHeaderMessage/>
                <#elseif group.header.headerText?has_content && group.header.headerText != '&nbsp;'>
                ${group.header.headerText}
                </#if>
            </span>
    </div>
    </#if>

    <#include "group.ftl" parse=true/>
    <@uif_group group=group/>

<!-- PAGE RELATED VARS -->
    <#if KualiForm.view.renderForm>
        <@spring.formHiddenInput id="pageId" path="KualiForm.view.currentPageId"/>

    </#if>

</#macro>
