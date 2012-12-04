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
package org.kuali.rice.krad.uif.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.rice.core.api.uif.RemotableAbstractWidget;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.kuali.rice.core.api.uif.RemotableCheckbox;
import org.kuali.rice.core.api.uif.RemotableCheckboxGroup;
import org.kuali.rice.core.api.uif.RemotableControlContract;
import org.kuali.rice.core.api.uif.RemotableDatepicker;
import org.kuali.rice.core.api.uif.RemotableHiddenInput;
import org.kuali.rice.core.api.uif.RemotableQuickFinder;
import org.kuali.rice.core.api.uif.RemotableRadioButtonGroup;
import org.kuali.rice.core.api.uif.RemotableSelect;
import org.kuali.rice.core.api.uif.RemotableSelectGroup;
import org.kuali.rice.core.api.uif.RemotableTextExpand;
import org.kuali.rice.core.api.uif.RemotableTextInput;
import org.kuali.rice.core.api.uif.RemotableTextarea;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.datadictionary.validation.constraint.ValidCharactersConstraint;
import org.kuali.rice.krad.keyvalues.KeyValuesFinder;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.container.LinkGroup;
import org.kuali.rice.krad.uif.container.NavigationGroup;
import org.kuali.rice.krad.uif.container.PageGroup;
import org.kuali.rice.krad.uif.container.TabGroup;
import org.kuali.rice.krad.uif.container.TreeGroup;
import org.kuali.rice.krad.uif.control.CheckboxControl;
import org.kuali.rice.krad.uif.control.CheckboxGroupControl;
import org.kuali.rice.krad.uif.control.Control;
import org.kuali.rice.krad.uif.control.FileControl;
import org.kuali.rice.krad.uif.control.HiddenControl;
import org.kuali.rice.krad.uif.control.MultiValueControl;
import org.kuali.rice.krad.uif.control.RadioGroupControl;
import org.kuali.rice.krad.uif.control.SelectControl;
import org.kuali.rice.krad.uif.control.SizedControl;
import org.kuali.rice.krad.uif.control.TextAreaControl;
import org.kuali.rice.krad.uif.control.TextControl;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.element.Action;
import org.kuali.rice.krad.uif.element.Header;
import org.kuali.rice.krad.uif.element.Iframe;
import org.kuali.rice.krad.uif.element.Image;
import org.kuali.rice.krad.uif.element.Label;
import org.kuali.rice.krad.uif.element.Message;
import org.kuali.rice.krad.uif.element.ValidationMessages;
import org.kuali.rice.krad.uif.field.DataField;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.field.MessageField;
import org.kuali.rice.krad.uif.field.SpaceField;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.field.GenericField;
import org.kuali.rice.krad.uif.field.ImageField;
import org.kuali.rice.krad.uif.field.LinkField;
import org.kuali.rice.krad.uif.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating new UIF components from their base definitions
 * in the dictionary
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ComponentFactory {

    private static Log LOG = LogFactory.getLog(ComponentFactory.class);

    protected static final String TEXT_CONTROL = "Uif-TextControl";
    protected static final String CHECKBOX_CONTROL = "Uif-CheckboxControl";
    protected static final String HIDDEN_CONTROL = "Uif-HiddenControl";
    protected static final String TEXTAREA_CONTROL = "Uif-TextAreaControl";
    protected static final String SELECT_CONTROL = "Uif-DropdownControl";
    protected static final String CHECKBOX_GROUP_CONTROL = "Uif-VerticalCheckboxesControl";
    protected static final String CHECKBOX_GROUP_CONTROL_HORIZONTAL = "Uif-HorizontalCheckboxesControl";
    protected static final String RADIO_GROUP_CONTROL = "Uif-VerticalRadioControl";
    protected static final String RADIO_GROUP_CONTROL_HORIZONTAL = "Uif-HorizontalRadioControl";
    protected static final String FILE_CONTROL = "Uif-FileControl";
    protected static final String DATE_CONTROL = "Uif-DateControl";
    protected static final String USER_CONTROL = "Uif-KimPersonControl";
    protected static final String GROUP_CONTROL = "Uif-KimGroupControl";

    protected static final String DATA_FIELD = "Uif-DataField";
    protected static final String INPUT_FIELD = "Uif-InputField";
    protected static final String ERRORS_FIELD = "Uif-FieldValidationMessages";
    protected static final String ACTION = "Uif-PrimaryActionButton";
    protected static final String ACTION_LINK = "Uif-ActionLink";
    protected static final String LINK_FIELD = "Uif-LinkField";
    protected static final String IFRAME = "Uif-Iframe";
    protected static final String IMAGE_FIELD = "Uif-ImageField";
    protected static final String SPACE_FIELD = "Uif-SpaceField";
    protected static final String GENERIC_FIELD = "Uif-CustomTemplateField";
    protected static final String IMAGE = "Uif-Image";
    protected static final String LABEL = "Uif-Label";
    protected static final String MESSAGE = "Uif-Message";
    protected static final String MESSAGE_FIELD = "Uif-MessageField";
    protected static final String COLLECTION_GROUPING_FIELD = "Uif-ColGroupingField";
    protected static final String FIELD_GROUP = "Uif-VerticalFieldGroup";
    protected static final String HORIZONTAL_FIELD_GROUP = "Uif-HorizontalFieldGroup";

    protected static final String GROUP = "Uif-GroupBase";
    protected static final String VERTICAL_BOX_GROUP = "Uif-VerticalBoxGroup";
    protected static final String HORIZONTAL_BOX_GROUP = "Uif-HorizontalBoxGroup";
    protected static final String PAGE_GROUP = "Uif-Page";
    protected static final String GROUP_GRID_LAYOUT = "Uif-GridSection";
    protected static final String GROUP_BODY_ONLY = "Uif-BoxGroupBase";
    protected static final String GROUP_GRID_BODY_ONLY = "Uif-GridGroup";
    protected static final String TAB_GROUP = "Uif-TabSection";
    protected static final String NAVIGATION_GROUP = "Uif-NavigationGroupBase";
    protected static final String TREE_GROUP = "Uif-TreeSection";
    protected static final String LINK_GROUP = "Uif-LinkGroup";
    protected static final String COLLECTION_GROUP = "Uif-StackedCollectionSection";
    protected static final String COLLECTION_GROUP_TABLE_LAYOUT = "Uif-TableCollectionSection";
    protected static final String LIST_GROUP = "Uif-ListCollectionSection";

    protected static final String HEADER = "Uif-HeaderFieldBase";
    protected static final String FOOTER = "Uif-FooterBase";
    protected static final String FOOTER_SAVECLOSECANCEL = "Uif-FormPageFooter";

    protected static final String CONSTRAINT_MESSAGE = "Uif-ConstraintMessage";
    protected static final String INSTRUCTIONAL_MESSAGE = "Uif-InstructionalMessage";
    protected static final String HELP_ACTION = "Uif-HelpAction";
    protected static final String IMAGE_CAPTION_HEADER = "Uif-ImageCaptionHeader";
    protected static final String IMAGE_CUTLINE_MESSAGE = "Uif-ImageCutineMessage";

    private static Map<String, Component> cache = new HashMap<String, Component>();

    /**
     * Gets a fresh copy of the component by the id passed in which used to look up the component in
     * the view index, then retrieve a new instance with initial state configured using the factory id
     *
     * @param id - id for the component in the view index
     * @return Component new instance
     */
    public static Component getNewInstanceForRefresh(View view, String id) {
        Component component = null;
        Component origComponent = view.getViewIndex().getComponentById(id);

        if (origComponent == null) {
            throw new RuntimeException(id + " not found in view index try setting p:forceSessionPersistence=\"true\" in xml");
        }

        if (view.getViewIndex().getInitialComponentStates().containsKey(origComponent.getBaseId())) {
            component = view.getViewIndex().getInitialComponentStates().get(origComponent.getBaseId());
            LOG.debug("getNewInstanceForRefresh: id '" + id + "' was found in initialStates");
        } else {
            component = (Component) KRADServiceLocatorWeb.getDataDictionaryService().getDictionaryObject(
                    origComponent.getBaseId());
            LOG.debug("getNewInstanceForRefresh: id '"
                    + id
                    + "' was NOT found in initialStates. New one fetched from DD");
        }

        if (component != null) {
            component = ComponentUtils.copyObject(component);
            component.setId(origComponent.getBaseId());
        }

        return component;
    }

    /**
     * Returns a new <code>Component</code> instance for the given bean id from the spring factory
     *
     * @param beanId - id of the bean definition
     * @return new component instance or null if no such component definition was found
     */
    public static Component getNewComponentInstance(String beanId) {
        Component component = null;
        if (cache.containsKey(beanId)) {
            component = ComponentUtils.copy(cache.get(beanId));
        } else {
            component = (Component) KRADServiceLocatorWeb.getDataDictionaryService().getDictionaryObject(beanId);

            // clear id before returning so duplicates do not occur
            component.setId(null);
            component.setBaseId(null);

            // populate property expressions from expression graph
            ExpressionUtils.populatePropertyExpressionsFromGraph(component, true);

            // add to cache
            // TODO: is this copy needed here? A copy is done when a request is made
            cache.put(beanId, ComponentUtils.copy(component));
        }

        return component;
    }

    /**
     * Retrieves a new Text control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return TextControl
     */
    public static TextControl getTextControl() {
        return (TextControl) getNewComponentInstance(TEXT_CONTROL);
    }

    /**
     * Retrieves a new Text area control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return TextAreaControl
     */
    public static TextAreaControl getTextAreaControl() {
        return (TextAreaControl) getNewComponentInstance(TEXTAREA_CONTROL);
    }

    /**
     * Retrieves a new checkbox control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return CheckboxControl
     */
    public static CheckboxControl getCheckboxControl() {
        return (CheckboxControl) getNewComponentInstance(CHECKBOX_CONTROL);
    }

    /**
     * Retrieves a new hidden control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return HiddenControl
     */
    public static HiddenControl getHiddenControl() {
        return (HiddenControl) getNewComponentInstance(HIDDEN_CONTROL);
    }

    /**
     * Retrieves a new select control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return SelectControl
     */
    public static SelectControl getSelectControl() {
        return (SelectControl) getNewComponentInstance(SELECT_CONTROL);
    }

    /**
     * Retrieves a new checkbox group control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * <p>
     * Return checkbox group set for vertical orientation
     * </p>
     *
     * @return CheckboxGroupControl
     */
    public static CheckboxGroupControl getCheckboxGroupControl() {
        return (CheckboxGroupControl) getNewComponentInstance(CHECKBOX_GROUP_CONTROL);
    }

    /**
     * Retrieves a new checkbox group control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * <p>
     * Return checkbox group set for horizontal orientation
     * </p>
     *
     * @return CheckboxGroupControl
     */
    public static CheckboxGroupControl getCheckboxGroupControlHorizontal() {
        return (CheckboxGroupControl) getNewComponentInstance(CHECKBOX_GROUP_CONTROL_HORIZONTAL);
    }

    /**
     * Retrieves a new radio group control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * <p>
     * Return radio group set for vertical orientation
     * </p>
     *
     * @return RadioGroupControl
     */
    public static RadioGroupControl getRadioGroupControl() {
        return (RadioGroupControl) getNewComponentInstance(RADIO_GROUP_CONTROL);
    }

    /**
     * Retrieves a new radio group control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * <p>
     * Return radio group set for horizontal orientation
     * </p>
     *
     * @return RadioGroupControl
     */
    public static RadioGroupControl getRadioGroupControlHorizontal() {
        return (RadioGroupControl) getNewComponentInstance(RADIO_GROUP_CONTROL_HORIZONTAL);
    }

    /**
     * Retrieves a new file control instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return FileControl
     */
    public static FileControl getFileControl() {
        return (FileControl) getNewComponentInstance(FILE_CONTROL);
    }

    /**
     * Retrieves a new text control instance from Spring (initialized by the bean definition
     * with the given id) configured for a date (enabled data picker)
     *
     * @return TextControl
     */
    public static TextControl getDateControl() {
        return (TextControl) getNewComponentInstance(DATE_CONTROL);
    }

    /**
     * Retrieves a new text control instance from Spring (initialized by the bean definition
     * with the given id) configured for KIM user input
     *
     * @return TextControl
     */
    public static TextControl getUserControl() {
        return (TextControl) getNewComponentInstance(USER_CONTROL);
    }

    /**
     * Retrieves a new text control instance from Spring (initialized by the bean definition
     * with the given id) configured for KIM group input
     *
     * @return TextControl
     */
    public static TextControl getGroupControl() {
        return (TextControl) getNewComponentInstance(GROUP_CONTROL);
    }

    /**
     * Retrieves a new data field instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return DataField
     */
    public static DataField getDataField() {
        return (DataField) getNewComponentInstance(DATA_FIELD);
    }

    /**
     * Retrieves a new data field instance from Spring (initialized by the bean definition
     * with the given id) and sets the property name and label to the given parameters
     *
     * @param propertyName - name of the property the data field should bind to
     * @param label - label for the field
     * @return DataField
     */
    public static DataField getDataField(String propertyName, String label) {
        DataField field = (DataField) getNewComponentInstance(DATA_FIELD);

        field.setPropertyName(propertyName);
        field.setLabel(label);

        return field;
    }

    /**
     * Retrieves a new input field instance from Spring (initialized by the bean definition
     * with the given id)
     *
     * @return InputField
     */
    public static InputField getInputField() {
        return (InputField) getNewComponentInstance(INPUT_FIELD);
    }

    /**
     * Retrieves a new input field instance from Spring (initialized by the bean definition
     * with the given id) and sets the property name and label to the given parameters
     *
     * @param propertyName - name of the property the input field should bind to
     * @param label - label for the field
     * @return InputField
     */
    public static InputField getInputField(String propertyName, String label) {
        InputField field = (InputField) getNewComponentInstance(INPUT_FIELD);

        field.setPropertyName(propertyName);
        field.setLabel(label);

        return field;
    }

    /**
     * Retrieves a new input field instance from Spring (initialized by the bean definition
     * with the given id) and sets the property name, control, and label to the given parameters
     *
     * @param propertyName - name of the property the input field should bind to
     * @param label - label for the field
     * @param controlType - enum that identifies the type of control to create for the input field
     * @return InputField
     */
    public static InputField getInputField(String propertyName, String label, UifConstants.ControlType controlType) {
        InputField field = (InputField) getNewComponentInstance(INPUT_FIELD);

        field.setPropertyName(propertyName);
        field.setLabel(label);
        field.setControl(getControl(controlType));

        return field;
    }

    /**
     * Retrieves a new input field instance from Spring (initialized by the bean definition
     * with the given id) and sets the property name, control, defaultValue, and label to the given parameters
     *
     * @param propertyName - name of the property the input field should bind to
     * @param label - label for the field
     * @param controlType - enum that identifies the type of control to create for the input field
     * @param defaultValue - default value for the property backing the input field
     * @return InputField
     */
    public static InputField getInputField(String propertyName, String label, UifConstants.ControlType controlType,
            String defaultValue) {
        InputField field = (InputField) getNewComponentInstance(INPUT_FIELD);

        field.setPropertyName(propertyName);
        field.setLabel(label);
        field.setControl(getControl(controlType));
        field.setDefaultValue(defaultValue);

        return field;
    }

    /**
     * Retrieves a new input field instance from Spring (initialized by the bean definition
     * with the given id) and sets the property name, control, options finder, and label to the given parameters
     *
     * @param propertyName - name of the property the input field should bind to
     * @param label - label for the field
     * @param controlType - enum that identifies the type of control to create for the input field
     * @param optionsFinderClass - class that will provide options for the control (assume control type is multi-value)
     * @return InputField
     */
    public static InputField getInputField(String propertyName, String label, UifConstants.ControlType controlType,
            Class<? extends KeyValuesFinder> optionsFinderClass) {
        InputField field = (InputField) getNewComponentInstance(INPUT_FIELD);

        field.setPropertyName(propertyName);
        field.setLabel(label);
        field.setControl(getControl(controlType));
        field.setOptionsFinderClass(optionsFinderClass);

        return field;
    }

    /**
     * Retrieves a new input field instance from Spring (initialized by the bean definition
     * with the given id) and sets the property name, control, options, and label to the given parameters
     *
     * @param propertyName - name of the property the input field should bind to
     * @param label - label for the field
     * @param controlType - enum that identifies the type of control to create for the input field
     * @param options - list of key value objects to set as the controls options
     * @return InputField
     */
    public static InputField getInputField(String propertyName, String label, UifConstants.ControlType controlType,
            List<KeyValue> options) {
        InputField field = (InputField) getNewComponentInstance(INPUT_FIELD);

        field.setPropertyName(propertyName);
        field.setLabel(label);

        Control control = getControl(controlType);
        if (control instanceof MultiValueControl) {
            ((MultiValueControl) control).setOptions(options);
        } else {
            throw new RuntimeException("Control is not instance of multi-value control, cannot set options");
        }

        return field;
    }

    /**
     * Retrieves a new input field instance from Spring (initialized by the bean definition
     * with the given id) and sets the property name, control, size, min and max length,
     * and label to the given parameters
     *
     * @param propertyName - name of the property the input field should bind to
     * @param label - label for the field
     * @param controlType - enum that identifies the type of control to create for the input field
     * @param size - size for the control
     * @param maxLength - max length for the field's value (also used for the control)
     * @param minLength - min length for the field's value (also used for the control)
     * @return InputField
     */
    public static InputField getInputField(String propertyName, String label, UifConstants.ControlType controlType,
            int size, int maxLength, int minLength) {
        InputField field = (InputField) getNewComponentInstance(INPUT_FIELD);

        field.setPropertyName(propertyName);
        field.setLabel(label);

        Control control = getControl(controlType);
        if (control instanceof SizedControl) {
            ((SizedControl) control).setSize(size);
        } else {
            throw new RuntimeException("Control does not support the size property");
        }

        field.setMaxLength(maxLength);
        field.setMinLength(minLength);

        return field;
    }

    /**
     * Builds a new <code>InputField</code> from the properties set on the
     * given <code>RemotableAttributeField</code>
     *
     * <p>
     * Note the returned InputField will not be initialized yet. Its state will be that of the initial
     * object returned from the UIF dictionary with the properties set from the remotable attribute field, thus it
     * is really just a more configuration complete field
     * </p>
     *
     * @return AttributeField instance built from remotable field
     */
    public static InputField translateRemotableField(RemotableAttributeField remotableField) {
        InputField inputField = getInputField();

        inputField.setPropertyName(remotableField.getName());
        inputField.setShortLabel(remotableField.getShortLabel());
        inputField.setLabel(remotableField.getLongLabel());
        inputField.setConstraintText(remotableField.getConstraintText());
        inputField.setUppercaseValue(remotableField.isForceUpperCase());
        inputField.setMinLength(remotableField.getMinLength());
        inputField.setMaxLength(remotableField.getMaxLength());

        // why are exclusive min and max strings?
        if (remotableField.getMinValue() != null) {
            inputField.setExclusiveMin(remotableField.getMinValue().toString());
        }
        if (remotableField.getMaxValue() != null) {
            inputField.setInclusiveMax(remotableField.getMaxValue().toString());
        }
        inputField.setRequired(remotableField.isRequired());

        if ((remotableField.getDefaultValues() != null) && !remotableField.getDefaultValues().isEmpty()) {
            inputField.setDefaultValue(remotableField.getDefaultValues().iterator().next());
        }

        if (StringUtils.isNotBlank(remotableField.getRegexConstraint())) {
            ValidCharactersConstraint constraint = new ValidCharactersConstraint();
            constraint.setValue(remotableField.getRegexConstraint());
            inputField.setValidCharactersConstraint(constraint);
            // TODO: how to deal with remotable field regexContraintMsg?
        }

        RemotableDatepicker remotableDatepicker = null;
        RemotableTextExpand remotableTextExpand = null;
        RemotableQuickFinder remotableQuickFinder = null;
        for (RemotableAbstractWidget remoteWidget : remotableField.getWidgets()) {
            if (remoteWidget instanceof RemotableDatepicker) {
                remotableDatepicker = (RemotableDatepicker) remoteWidget;
            } else if (remoteWidget instanceof RemotableTextExpand) {
                remotableTextExpand = (RemotableTextExpand) remoteWidget;
            } else if (remoteWidget instanceof RemotableQuickFinder) {
                remotableQuickFinder = (RemotableQuickFinder) remoteWidget;
            }
        }

        if (remotableQuickFinder != null) {
            inputField.getQuickfinder().setBaseLookupUrl(remotableQuickFinder.getBaseLookupUrl());
            inputField.getQuickfinder().setDataObjectClassName(remotableQuickFinder.getDataObjectClass());
            inputField.getQuickfinder().setLookupParameters(remotableQuickFinder.getLookupParameters());
            inputField.getQuickfinder().setFieldConversions(remotableQuickFinder.getFieldConversions());
        }

        if (remotableField.getControl() != null) {
            Control control = null;

            RemotableControlContract remotableControl = remotableField.getControl();
            if (remotableControl instanceof RemotableHiddenInput) {
                control = getHiddenControl();
            } else if (remotableControl instanceof RemotableRadioButtonGroup) {
                RemotableRadioButtonGroup remotableRadioButtonGroup = (RemotableRadioButtonGroup) remotableControl;
                control = getRadioGroupControl();
                ((RadioGroupControl) control).setOptions(buildKeyValuePairs(remotableRadioButtonGroup.getKeyLabels()));
            } else if (remotableControl instanceof RemotableSelect) {
                RemotableSelect remotableSelect = (RemotableSelect) remotableControl;
                control = getSelectControl();

                Map<String, String> keyLabels = new HashMap<String, String>();
                if ((remotableSelect.getGroups() != null) && (!remotableSelect.getGroups().isEmpty())) {
                    for (RemotableSelectGroup remotableSelectGroup : remotableSelect.getGroups()) {
                        keyLabels.putAll(remotableSelectGroup.getKeyLabels());
                    }
                } else {
                    keyLabels = remotableSelect.getKeyLabels();
                }

                ((SelectControl) control).setOptions(buildKeyValuePairs(keyLabels));
                if (remotableSelect.getSize() != null) {
                    ((SelectControl) control).setSize(remotableSelect.getSize());
                }
                ((SelectControl) control).setMultiple(remotableSelect.isMultiple());
            } else if (remotableControl instanceof RemotableCheckboxGroup) {
                RemotableCheckboxGroup remotableCheckboxGroup = (RemotableCheckboxGroup) remotableControl;
                control = getCheckboxGroupControl();
                ((CheckboxGroupControl) control).setOptions(buildKeyValuePairs(remotableCheckboxGroup.getKeyLabels()));
            } else if (remotableControl instanceof RemotableCheckbox) {
                control = getCheckboxControl();
            } else if (remotableControl instanceof RemotableTextarea) {
                RemotableTextarea remotableTextarea = (RemotableTextarea) remotableControl;
                control = getTextAreaControl();

                if (remotableTextExpand != null) {
                    ((TextAreaControl) control).setTextExpand(true);
                }
                ((TextAreaControl) control).setRows(remotableTextarea.getRows());
                ((TextAreaControl) control).setCols(remotableTextarea.getCols());
                ((TextAreaControl) control).setWatermarkText(remotableTextarea.getWatermark());

            } else if (remotableControl instanceof RemotableTextInput) {
                RemotableTextInput remotableTextInput = (RemotableTextInput) remotableControl;

                if (remotableDatepicker != null) {
                    control = getDateControl();
                } else {
                    control = getTextControl();
                }

                if (remotableTextExpand != null) {
                    ((TextAreaControl) control).setTextExpand(true);
                }
                ((TextControl) control).setSize(remotableTextInput.getSize());
                ((TextControl) control).setWatermarkText(remotableTextInput.getWatermark());
            }

            inputField.setControl(control);
        }

        return inputField;
    }

    /**
     * For each remotable field in the given list creates a new {@link org.kuali.rice.krad.uif.field.InputField}
     * instance and sets the
     * corresponding properties from the remotable instance
     *
     * @param remotableFields - list of remotable fields to translate
     * @return List<AttributeField> list of attribute fields built from the remotable field properties
     */
    public static List<InputField> translateRemotableFields(List<RemotableAttributeField> remotableFields) {
        List<InputField> inputFields = new ArrayList<InputField>();

        for (RemotableAttributeField remotableField : remotableFields) {
            inputFields.add(translateRemotableField(remotableField));
        }

        return inputFields;
    }

    /**
     * For each option in the given list, create a new {@link org.kuali.rice.core.api.util.KeyValue} instance
     *
     * @param optionsMap - list of options
     * @return List<KeyValue> list of key values built from the list of options
     */
    protected static List<KeyValue> buildKeyValuePairs(Map<String, String> optionsMap) {
        List<KeyValue> options = new ArrayList<KeyValue>();

        for (Map.Entry<String, String> optionEntry : optionsMap.entrySet()) {
            KeyValue keyValue = new ConcreteKeyValue(optionEntry.getKey(), optionEntry.getValue());
            options.add(keyValue);
        }

        return options;
    }

    /**
     * Gets the control
     *
     * @param controlType
     * @return Control the control based on the control type
     */
    protected static Control getControl(UifConstants.ControlType controlType) {
        Control control = null;
        switch (controlType) {
            case CHECKBOX:
                control = getCheckboxControl();
            case CHECKBOXGROUP:
                control = getCheckboxGroupControl();
            case FILE:
                control = getFileControl();
            case GROUP:
                control = getGroupControl();
            case HIDDEN:
                control = getHiddenControl();
            case RADIOGROUP:
                control = getRadioGroupControl();
            case SELECT:
                control = getSelectControl();
            case TEXTAREA:
                control = getTextAreaControl();
            case TEXT:
                control = getTextControl();
            case USER:
                control = getUserControl();
        }

        return control;
    }

    /**
     * Gets the errors field
     *
     * @return ValidationMessages errors field
     */
    public static ValidationMessages getErrorsField() {
        return (ValidationMessages) getNewComponentInstance(ERRORS_FIELD);
    }

    /**
     * Gets the action
     *
     * @return Action action
     */
    public static Action getAction() {
        return (Action) getNewComponentInstance(ACTION);
    }

    /**
     * Gets the action link
     *
     * @return Action action link
     */
    public static Action getActionLink() {
        return (Action) getNewComponentInstance(ACTION_LINK);
    }

    /**
     * Gets the link field
     *
     * @return LinkField link field
     */
    public static LinkField getLinkField() {
        return (LinkField) getNewComponentInstance(LINK_FIELD);
    }

    /**
     * Gets the iframe
     *
     * @return Iframe iframe
     */
    public static Iframe getIframe() {
        return (Iframe) getNewComponentInstance(IFRAME);
    }

    /**
     * Gets the image field
     *
     * @return ImageField image field
     */
    public static ImageField getImageField() {
        return (ImageField) getNewComponentInstance(IMAGE_FIELD);
    }

    /**
     * Gets the image component
     *
     * @return ImageField image field
     */
    public static Image getImage() {
        return (Image) getNewComponentInstance(IMAGE);
    }

    /**
     * Gets the space field
     *
     * @return SpaceField space field
     */
    public static SpaceField getSpaceField() {
        return (SpaceField) getNewComponentInstance(SPACE_FIELD);
    }

    /**
     * Gets the generic field
     *
     * @return GenericField generic field
     */
    public static GenericField getGenericField() {
        return (GenericField) getNewComponentInstance(GENERIC_FIELD);
    }

    /**
     * Gets the label
     *
     * @return Label label
     */
    public static Label getLabel() {
        return (Label) getNewComponentInstance(LABEL);
    }

    /**
     * Gets the message
     *
     * @return Message message
     */
    public static Message getMessage() {
        return (Message) getNewComponentInstance(MESSAGE);
    }

    /**
     * Gets the message field
     *
     * @return MessageField message field
     */
    public static MessageField getMessageField() {
        return (MessageField) getNewComponentInstance(MESSAGE_FIELD);
    }

    /**
     * Gets the collection grouping field
     *
     * @return MessageField message field
     */
    public static MessageField getColGroupingField() {
        return (MessageField) getNewComponentInstance(COLLECTION_GROUPING_FIELD);
    }

    /**
     * Gets the field group
     *
     * @return FieldGroup field group
     */
    public static FieldGroup getFieldGroup() {
        return (FieldGroup) getNewComponentInstance(FIELD_GROUP);
    }

    /**
     * Gets the horizontal field group
     *
     * @return FieldGroup horizontal field group
     */
    public static FieldGroup getHorizontalFieldGroup() {
        return (FieldGroup) getNewComponentInstance(HORIZONTAL_FIELD_GROUP);
    }

    /**
     * Gets the group
     *
     * @return Group group
     */
    public static Group getGroup() {
        return (Group) getNewComponentInstance(GROUP);
    }

    /**
     * Gets the vertical box group
     *
     * @return Group group
     */
    public static Group getVerticalBoxGroup() {
        return (Group) getNewComponentInstance(VERTICAL_BOX_GROUP);
    }

    /**
     * Gets the horizontal box group
     *
     * @return Group group
     */
    public static Group getHorizontalBoxGroup() {
        return (Group) getNewComponentInstance(HORIZONTAL_BOX_GROUP);
    }

    /**
     * Gets the page group
     *
     * @return PageGroup page group
     */
    public static PageGroup getPageGroup() {
        return (PageGroup) getNewComponentInstance(PAGE_GROUP);
    }

    /**
     * Gets the group grid layout
     *
     * @return Group group grid layout
     */
    public static Group getGroupGridLayout() {
        return (Group) getNewComponentInstance(GROUP_GRID_LAYOUT);
    }

    /**
     * Gets the group body only
     *
     * @return Group group body only
     */
    public static Group getGroupBodyOnly() {
        return (Group) getNewComponentInstance(GROUP_BODY_ONLY);
    }

    /**
     * Gets the group grid body only
     *
     * @return Group group grid body only
     */
    public static Group getGroupGridBodyOnly() {
        return (Group) getNewComponentInstance(GROUP_GRID_BODY_ONLY);
    }

    /**
     * Gets the tab group
     *
     * @return TabGroup tab group
     */
    public static TabGroup getTabGroup() {
        return (TabGroup) getNewComponentInstance(TAB_GROUP);
    }

    /**
     * Gets the navigation group
     *
     * @return NavigationGroup navigation group
     */
    public static NavigationGroup getNavigationGroup() {
        return (NavigationGroup) getNewComponentInstance(NAVIGATION_GROUP);
    }

    /**
     * Gets the tree group
     *
     * @return TreeGroup tree group
     */
    public static TreeGroup getTreeGroup() {
        return (TreeGroup) getNewComponentInstance(TREE_GROUP);
    }

    /**
     * Gets the link group
     *
     * @return LinkGroup link group
     */
    public static LinkGroup getLinkGroup() {
        return (LinkGroup) getNewComponentInstance(LINK_GROUP);
    }

    /**
     * Gets the collection group
     *
     * @return CollectionGroup collection group
     */
    public static CollectionGroup getCollectionGroup() {
        return (CollectionGroup) getNewComponentInstance(COLLECTION_GROUP);
    }

    /**
     * Gets the collection group table layout
     *
     * @return CollectionGroup collection group table layout
     */
    public static CollectionGroup getCollectionGroupTableLayout() {
        return (CollectionGroup) getNewComponentInstance(COLLECTION_GROUP_TABLE_LAYOUT);
    }

    /**
     * Gets the list group
     *
     * @return CollectionGroup list group
     */
    public static CollectionGroup getListGroup() {
        return (CollectionGroup) getNewComponentInstance(LIST_GROUP);
    }

    /**
     * Gets the header
     *
     * @return Header header
     */
    public static Header getHeader() {
        return (Header) getNewComponentInstance(HEADER);
    }

    /**
     * Gets the footer
     *
     * @return Group footer
     */
    public static Group getFooter() {
        return (Group) getNewComponentInstance(FOOTER);
    }

    /**
     * Gets the footer save/close/cancel
     *
     * @return Group footer save/close/cancel
     */
    public static Group getFooterSaveCloseCancel() {
        return (Group) getNewComponentInstance(FOOTER_SAVECLOSECANCEL);
    }

    /**
     * Gets the default action component configured for help
     *
     * @return Action for help display
     */
    public static Action getHelpAction() {
        return (Action) getNewComponentInstance(HELP_ACTION);
    }

    /**
     * Gets the default constraint message configuration
     *
     * @return Message component for constraint messages
     */
    public static Message getConstraintMessage() {
        return (Message) getNewComponentInstance(CONSTRAINT_MESSAGE);
    }

    /**
     * Gets the default instructional message configuration
     *
     * @return Message component for instructional messages
     */
    public static Message getInstructionalMessage() {
        return (Message) getNewComponentInstance(INSTRUCTIONAL_MESSAGE);
    }

    /**
     * Gets the default image caption header configuration
     *
     * @return Header component for image caption headers
     */
    public static Header getImageCaptionHeader() {
        return (Header) getNewComponentInstance(IMAGE_CAPTION_HEADER);
    }

    /**
     * Gets the default image cutline message configuration
     *
     * @return Message component for image cutlines messages
     */
    public static Message getImageCutlineMessage() {
        return (Message) getNewComponentInstance(IMAGE_CUTLINE_MESSAGE);
    }
}
