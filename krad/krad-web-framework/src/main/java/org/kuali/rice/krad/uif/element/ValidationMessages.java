/**
 * Copyright 2005-2012 The Kuali Foundation
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
package org.kuali.rice.krad.uif.element;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.container.ContainerBase;
import org.kuali.rice.krad.uif.container.PageGroup;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.util.ErrorMessage;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.util.MessageMap;
import org.springframework.util.AutoPopulatingList;

import java.beans.PropertyEditor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Field that displays error, warning, and info messages for the keys that are
 * matched. By default, an ValidationMessages will match on id and bindingPath (if this
 * ValidationMessages is for an InputField), but can be set to match on
 * additionalKeys and nested components keys (of the its parentComponent).
 *
 * In addition, there are a variety of options which can be toggled to effect
 * the display of these messages during both client and server side validation
 * display. See documentation on each get method for more details on the effect
 * of each option.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ValidationMessages extends ContentElementBase {
    private static final long serialVersionUID = 780940788435330077L;

    private List<String> additionalKeysToMatch;

    private boolean displayMessages;

    // Error messages
    private List<String> errors;
    private List<String> warnings;
    private List<String> infos;

    /**
     * PerformFinalize will generate the messages and counts used by the
     * errorsField based on the keys that were matched from the MessageMap for
     * this ValidationMessages. It will also set up nestedComponents of its
     * parentComponent correctly based on the flags that were chosen for this
     * ValidationMessages.
     *
     * @see org.kuali.rice.krad.uif.field.FieldBase#performFinalize(org.kuali.rice.krad.uif.view.View,
     *      java.lang.Object, org.kuali.rice.krad.uif.component.Component)
     */
    @Override
    public void performFinalize(View view, Object model, Component parent) {
        super.performFinalize(view, model, parent);

        generateMessages(true, view, model, parent);
    }

    /**
     * Generates the messages based on the content in the messageMap
     *
     * @param reset - true to reset the errors, warnings, and info lists
     * @param view - the current View
     * @param model - the current model
     * @param parent - the parent of this ValidationMessages
     */
    public void generateMessages(boolean reset, View view, Object model, Component parent) {
        if (reset) {
            errors = new ArrayList<String>();
            warnings = new ArrayList<String>();
            infos = new ArrayList<String>();
        }

        List<String> masterKeyList = getKeys(parent);
        MessageMap messageMap = GlobalVariables.getMessageMap();

        String parentContainerId = "";
        Object parentContainer = parent.getContext().get("parent");
        if (parentContainer != null && (parentContainer instanceof Container
                || parentContainer instanceof FieldGroup)) {
            parentContainerId = ((Component) parentContainer).getId();
        }

        //Add identifying data attributes
        this.addDataAttribute("messagesFor", parent.getId());
        parent.addDataAttribute("parent", parentContainerId);

        //Handle the special FieldGroup case - adds the FieldGroup itself to ids handled by this group (this must
        //be a group if its parent is FieldGroup)
        if (parentContainer != null && parentContainer instanceof FieldGroup) {
            masterKeyList.add(parentContainerId);
        }

        //Check for message keys that are not matched anywhere on the page - these unmatched messages must still be
        //displayed at the page level
        if (parent instanceof PageGroup) {
            Map<String, PropertyEditor> propertyEditors = view.getViewIndex().getFieldPropertyEditors();
            Map<String, PropertyEditor> securePropertyEditors = view.getViewIndex().getSecureFieldPropertyEditors();
            List<String> allPossibleKeys = new ArrayList<String>(propertyEditors.keySet());
            allPossibleKeys.addAll(securePropertyEditors.keySet());

            this.addNestedGroupKeys(allPossibleKeys, parent);
            if (additionalKeysToMatch != null) {
                allPossibleKeys.addAll(additionalKeysToMatch);
            }
            if (StringUtils.isNotBlank(parent.getId())) {
                allPossibleKeys.add(parent.getId());
            }

            Set<String> messageKeys = new HashSet<String>();
            messageKeys.addAll(messageMap.getAllPropertiesWithErrors());
            messageKeys.addAll(messageMap.getAllPropertiesWithWarnings());
            messageKeys.addAll(messageMap.getAllPropertiesWithInfo());

            messageKeys.removeAll(allPossibleKeys);

            masterKeyList.addAll(messageKeys);
        }

        for (String key : masterKeyList) {
            errors.addAll(getMessages(view, key, messageMap.getErrorMessagesForProperty(key, true)));
            warnings.addAll(getMessages(view, key, messageMap.getWarningMessagesForProperty(key, true)));
            infos.addAll(getMessages(view, key, messageMap.getInfoMessagesForProperty(key, true)));
        }
    }

    /**
     * Gets all the messages from the list of lists passed in (which are
     * lists of ErrorMessages associated to the key) and uses the configuration
     * service to get the message String associated. This will also combine
     * error messages per a field if that option is turned on. If
     * displayFieldLabelWithMessages is turned on, it will also find the label
     * by key passed in.
     *
     * @param view
     * @param key
     * @param lists
     * @return
     */
    private List<String> getMessages(View view, String key, List<AutoPopulatingList<ErrorMessage>> lists) {
        List<String> result = new ArrayList<String>();
        for (List<ErrorMessage> errorList : lists) {
            if (errorList != null && StringUtils.isNotBlank(key)) {
                ConfigurationService configService = KRADServiceLocator.getKualiConfigurationService();

                for (ErrorMessage e : errorList) {
                    String message = KRADUtils.getMessage(configService, e, true);

                    result.add(message);
                }
            }
        }

        return result;
    }

    /**
     * Gets all the keys associated to this ValidationMessages. This includes the id of
     * the parent component, additional keys to match, and the bindingPath if
     * this is an ValidationMessages for an InputField. These are the keys that are
     * used to match errors with their component and display them as part of its
     * ValidationMessages.
     *
     * @return
     */
    protected List<String> getKeys(Component parent) {
        List<String> keyList = new ArrayList<String>();
        if (additionalKeysToMatch != null) {
            keyList.addAll(additionalKeysToMatch);
        }
        if (StringUtils.isNotBlank(parent.getId())) {
            keyList.add(parent.getId());
        }
        if (parent instanceof InputField) {
            if (((InputField) parent).getBindingInfo() != null && StringUtils.isNotEmpty(
                    ((InputField) parent).getBindingInfo().getBindingPath())) {
                keyList.add(((InputField) parent).getBindingInfo().getBindingPath());
            }
        }

        return keyList;
    }

    /**
     * Adds all group keys of this component (starting from this component itself) by calling getKeys on each of
     * its nested group's ValidationMessages and adding them to the list.
     *
     * @param keyList
     * @param component
     */
    private void addNestedGroupKeys(Collection<String> keyList, Component component) {
        for (Component c : component.getComponentsForLifecycle()) {
            ValidationMessages ef = null;
            if (c instanceof ContainerBase) {
                ef = ((ContainerBase) c).getValidationMessages();
            } else if (c instanceof FieldGroup) {
                ef = ((FieldGroup) c).getGroup().getValidationMessages();
            }
            if (ef != null) {
                keyList.addAll(ef.getKeys(c));
                addNestedGroupKeys(keyList, c);
            }
        }
    }

    /**
     * AdditionalKeysToMatch is an additional list of keys outside of the
     * default keys that will be matched when messages are returned after a form
     * is submitted. These keys are only used for displaying messages generated
     * by the server and have no effect on client side validation error display.
     *
     * @return the additionalKeysToMatch
     */
    public List<String> getAdditionalKeysToMatch() {
        return this.additionalKeysToMatch;
    }

    /**
     * Convenience setter for additional keys to match that takes a string argument and
     * splits on comma to build the list
     *
     * @param additionalKeysToMatch String to parse
     */
    public void setAdditionalKeysToMatch(String additionalKeysToMatch) {
        if (StringUtils.isNotBlank(additionalKeysToMatch)) {
            this.additionalKeysToMatch = Arrays.asList(StringUtils.split(additionalKeysToMatch, ","));
        }
    }

    /**
     * @param additionalKeysToMatch the additionalKeysToMatch to set
     */
    public void setAdditionalKeysToMatch(List<String> additionalKeysToMatch) {
        this.additionalKeysToMatch = additionalKeysToMatch;
    }

    /**
     * <p>If true, error, warning, and info messages will be displayed (provided
     * they are also set to display). Otherwise, no messages for this
     * ValidationMessages container will be displayed (including ones set to display).
     * This is a global display on/off switch for all messages.</p>
     *
     * <p>Other areas of the screen react to
     * a display flag being turned off at a certain level, if display is off for a field, the next
     * level up will display that fields full message text, and if display is off at a section the
     * next section up will display those messages nested in a sublist.</p>
     *
     * @return the displayMessages
     */
    public boolean isDisplayMessages() {
        return this.displayMessages;
    }

    /**
     * @param displayMessages the displayMessages to set
     */
    public void setDisplayMessages(boolean displayMessages) {
        this.displayMessages = displayMessages;
    }

    /**
     * The list of error messages found for the keys that were matched on this
     * ValidationMessages This is generated and cannot be set
     *
     * @return the errors
     */
    public List<String> getErrors() {
        return this.errors;
    }

    /**
     * The list of warning messages found for the keys that were matched on this
     * ValidationMessages This is generated and cannot be set
     *
     * @return the warnings
     */
    public List<String> getWarnings() {
        return this.warnings;
    }

    /**
     * The list of info messages found for the keys that were matched on this
     * ValidationMessages This is generated and cannot be set
     *
     * @return the infos
     */
    public List<String> getInfos() {
        return this.infos;
    }

}
