/*
 * Copyright 2006-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.rice.krad.document;

import org.kuali.rice.core.api.exception.RiceRuntimeException;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.view.ViewAuthorizer;
import org.kuali.rice.krad.uif.view.ViewAuthorizerBase;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.web.form.LookupForm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all TransactionalDocumentAuthorizers.
 */
public class TransactionalDocumentAuthorizerBase extends ViewAuthorizerBase {
    private static final long serialVersionUID = 3755133642834256283L;

    /**
     * Override to check the for permissions of type 'Look Up Records' in addition to the open view check
     * done in super
     */
    @Override
    public boolean canOpenView(View view, ViewModel model, Person user) {
        boolean canOpen = super.canOpenView(view, model, user);

        if (canOpen) {
            LookupForm lookupForm = (LookupForm) model;

            Map<String, String> additionalPermissionDetails;
            try {
                additionalPermissionDetails = KRADUtils.getNamespaceAndComponentSimpleName(Class.forName(
                        lookupForm.getDataObjectClassName()));
            } catch (ClassNotFoundException e) {
                throw new RiceRuntimeException(
                        "Unable to create class for lookup class name: " + lookupForm.getDataObjectClassName());
            }

            if (permissionExistsByTemplate(model, KRADConstants.KNS_NAMESPACE,
                    KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS, additionalPermissionDetails)) {
                canOpen = isAuthorizedByTemplate(model, KRADConstants.KNS_NAMESPACE,
                        KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS, user.getPrincipalId(),
                        additionalPermissionDetails, null);
            }
        }

        return canOpen;
    }
}