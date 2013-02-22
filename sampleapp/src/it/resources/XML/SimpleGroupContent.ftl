<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright 2005-2011 The Kuali Foundation

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
<data xmlns="ns:workflow"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="ns:workflow resource:WorkflowData">
    <groups xmlns="ns:workflow/Group"
            xsi:schemaLocation="ns:workflow/Group resource:Group">
        <group>
            <!-- BEGIN: Generate Group -->
            <id>${groupId}</id>
            <namespace>${groupNamespace}</namespace>
            <description>${groupDesc}</description>
            <name>${groupName}</name>
            <members>
                <#if groupAddtlMembers?length &gt; 0 >
                <#list groupAddtlMembers?split(",") as member>
                    <principalName>${member}</principalName>
                </#list>
                </#if>
                <#list userCntBegin?number..userCnt?number as cnt>
                    <principalName>${userPrefix}${cnt}</principalName>
                </#list>
            </members>
            <!-- END: Generate Group -->
        </group>
    </groups>
</data>