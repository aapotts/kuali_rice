<#--

    Copyright 2005-2012 The Kuali Foundation

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

<html>
    <#-- need to render the pages errors since the component could have added errors for the page -->
    <@krad.template component=view.currentPage.validationMessages/>

    <#-- now render the updated component (or page) wrapped in an update div -->
    <div id="${Component.id}_update">
        <@krad.template componentUpdate=true component=Component/>

        <@krad.script value="${KualiForm.lightboxScript!}" component=Component/>

        <#-- show added growls -->
        <@krad.script value="${KualiForm.growlScript!}" component=Component/>

        <#-- set focus and perform jump to -->
        <@krad.script value="performFocusAndJumpTo(${view.currentPage.autoFocus?string}, false, false, '${KualiForm.focusId!}',
                             '${KualiForm.jumpToId!}', '${KualiForm.jumpToName!}');" component=Component/>
    </div>
</html>
