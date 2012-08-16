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
package org.kuali.rice.krad.uif.container;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.rice.core.api.mo.common.active.Inactivatable;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.UifPropertyPaths;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.component.ComponentSecurity;
import org.kuali.rice.krad.uif.component.DataBinding;
import org.kuali.rice.krad.uif.control.Control;
import org.kuali.rice.krad.uif.control.ControlBase;
import org.kuali.rice.krad.uif.element.Action;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.field.RemoteFieldsHolder;
import org.kuali.rice.krad.uif.layout.CollectionLayoutManager;
import org.kuali.rice.krad.uif.service.ExpressionEvaluatorService;
import org.kuali.rice.krad.uif.util.ComponentUtils;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.util.ScriptUtils;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.view.ViewAuthorizer;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.rice.krad.uif.view.ViewPresentationController;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.web.form.UifFormBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds out the {@code Field} instances for a collection group with a
 * series of steps that interact with the configured
 * {@code CollectionLayoutManager} to assemble the fields as necessary for
 * the layout
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class CollectionGroupBuilder implements Serializable {
    private static final long serialVersionUID = -4762031957079895244L;
    private static Log LOG = LogFactory.getLog(CollectionGroupBuilder.class);

    /**
     * Creates the {@code Field} instances that make up the table
     *
     * <p>
     * The corresponding collection is retrieved from the model and iterated
     * over to create the necessary fields. The binding path for fields that
     * implement {@code DataBinding} is adjusted to point to the collection
     * line it is apart of. For example, field 'number' of collection 'accounts'
     * for line 1 will be set to 'accounts[0].number', and for line 2
     * 'accounts[1].number'. Finally parameters are set on the line's action
     * fields to indicate what collection and line they apply to.
     * </p>
     *
     * @param view - View instance the collection belongs to
     * @param model - Top level object containing the data
     * @param collectionGroup - CollectionGroup component for the collection
     */
    public void build(View view, Object model, CollectionGroup collectionGroup) {
        // create add line
        if (collectionGroup.isRenderAddLine() && !collectionGroup.isReadOnly()) {
            buildAddLine(view, model, collectionGroup);
        }

        // get the collection for this group from the model
        List<Object> modelCollection = ObjectPropertyUtils.getPropertyValue(model,
                ((DataBinding) collectionGroup).getBindingInfo().getBindingPath());

        if (modelCollection != null) {
            // filter inactive model
            List<Integer> showIndexes = performCollectionFiltering(view, model, collectionGroup, modelCollection);

            if (collectionGroup.getDisplayCollectionSize() != -1 && showIndexes.size() > collectionGroup
                    .getDisplayCollectionSize()) {
                // remove all indexes in showIndexes beyond the collection's size limitation
                List<Integer> newShowIndexes = new ArrayList<Integer>();
                Integer counter = 0;

                for (int index = 0; index < showIndexes.size(); index++) {
                    newShowIndexes.add(showIndexes.get(index));

                    counter++;

                    if (counter == collectionGroup.getDisplayCollectionSize()) {
                        break;
                    }
                }

                showIndexes = newShowIndexes;
            }

            // for each collection row build the line fields
            for (int index = 0; index < modelCollection.size(); index++) {
                // display only records that passed filtering
                if (showIndexes.contains(index)) {
                    String bindingPathPrefix = collectionGroup.getBindingInfo().getBindingName() + "[" + index + "]";
                    if (StringUtils.isNotBlank(collectionGroup.getBindingInfo().getBindByNamePrefix())) {
                        bindingPathPrefix =
                                collectionGroup.getBindingInfo().getBindByNamePrefix() + "." + bindingPathPrefix;
                    }

                    Object currentLine = modelCollection.get(index);

                    // Default line actions - no client side validation
                    String actionScript = "performCollectionAction(this, '" + collectionGroup.getId() + "');";
                    List<Action> lineActions = initializeLineActions(collectionGroup.getLineActions(), view, model,
                            collectionGroup, currentLine, index, actionScript);

                    // Line actions with client side validation
                    String actionScriptValidatedLine = "validateAndPerformCollectionAction(this, '"
                            + collectionGroup.getId()
                            + "', '"
                            + collectionGroup.getPropertyName()
                            + "');";
                    List<Action> validatedLineActions = initializeLineActions(collectionGroup.getValidatedLineActions(),
                            view, model, collectionGroup, currentLine, index, actionScriptValidatedLine);

                    lineActions.addAll(validatedLineActions);

                    buildLine(view, model, collectionGroup, bindingPathPrefix, lineActions, false, currentLine, index);
                }
            }
        }
    }

    /**
     * Performs any filtering necessary on the collection before building the collection fields
     *
     * <p>
     * If showInactive is set to false and the collection line type implements {@code Inactivatable},
     * invokes the active collection filter. Then any {@link CollectionFilter} instances configured for the collection
     * group are invoked to filter the collection. Collections lines must pass all filters in order to be
     * displayed
     * </p>
     *
     * @param view - view instance that contains the collection
     * @param model - object containing the views data
     * @param collectionGroup - collection group component instance that will display the collection
     * @param collection - collection instance that will be filtered
     */
    protected List<Integer> performCollectionFiltering(View view, Object model, CollectionGroup collectionGroup,
            Collection<?> collection) {
        List<Integer> filteredIndexes = new ArrayList<Integer>();
        for (int i = 0; i < collection.size(); i++) {
            filteredIndexes.add(Integer.valueOf(i));
        }

        if (Inactivatable.class.isAssignableFrom(collectionGroup.getCollectionObjectClass()) && !collectionGroup
                .isShowInactiveLines()) {
            List<Integer> activeIndexes = collectionGroup.getActiveCollectionFilter().filter(view, model,
                    collectionGroup);
            filteredIndexes = ListUtils.intersection(filteredIndexes, activeIndexes);
        }

        for (CollectionFilter collectionFilter : collectionGroup.getFilters()) {
            List<Integer> indexes = collectionFilter.filter(view, model, collectionGroup);
            filteredIndexes = ListUtils.intersection(filteredIndexes, indexes);
            if (filteredIndexes.isEmpty()) {
                break;
            }
        }

        return filteredIndexes;
    }

    /**
     * Builds the fields for holding the collection add line and if necessary
     * makes call to setup the new line instance
     *
     * @param view - view instance the collection belongs to
     * @param collectionGroup - collection group the layout manager applies to
     * @param model - Object containing the view data, should extend UifFormBase
     * if using framework managed new lines
     */
    protected void buildAddLine(View view, Object model, CollectionGroup collectionGroup) {
        boolean addLineBindsToForm = false;

        // initialize new line if one does not already exist
        initializeNewCollectionLine(view, model, collectionGroup, false);

        // determine whether the add line binds to the generic form map or a
        // specified property
        if (StringUtils.isBlank(collectionGroup.getAddLinePropertyName())) {
            addLineBindsToForm = true;
        }

        String addLineBindingPath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        List<Action> actions = getAddLineActions(view, model, collectionGroup);

        Object addLine = ObjectPropertyUtils.getPropertyValue(model, addLineBindingPath);
        buildLine(view, model, collectionGroup, addLineBindingPath, actions, addLineBindsToForm, addLine, -1);
    }

    /**
     * Builds the field instances for the collection line. A copy of the
     * configured items on the {@code CollectionGroup} is made and adjusted
     * for the line (id and binding). Then a call is made to the
     * {@code CollectionLayoutManager} to assemble the line as necessary
     * for the layout
     *
     * @param view - view instance the collection belongs to
     * @param model - top level object containing the data
     * @param collectionGroup - collection group component for the collection
     * @param bindingPath - binding path for the line fields (if DataBinding)
     * @param actions - List of actions to set in the lines action column
     * @param bindToForm - whether the bindToForm property on the items bindingInfo
     * should be set to true (needed for add line)
     * @param currentLine - object instance for the current line, or null if add line
     * @param lineIndex - index of the line in the collection, or -1 if we are
     * building the add line
     */
    @SuppressWarnings("unchecked")
    protected void buildLine(View view, Object model, CollectionGroup collectionGroup, String bindingPath,
            List<Action> actions, boolean bindToForm, Object currentLine, int lineIndex) {
        if (lineIndex == -1 && collectionGroup.isRenderAddBlankLineButton()) {
            return;
        }

        CollectionLayoutManager layoutManager = (CollectionLayoutManager) collectionGroup.getLayoutManager();

        // copy group items for new line
        List<? extends Component> lineItems = null;
        String lineSuffix = null;
        if (lineIndex == -1) {
            lineItems = ComponentUtils.copyComponentList(collectionGroup.getAddLineItems(), null);
            lineSuffix = UifConstants.IdSuffixes.ADD_LINE;
        } else {
            lineItems = ComponentUtils.copyComponentList(collectionGroup.getItems(), null);
            lineSuffix = UifConstants.IdSuffixes.LINE + Integer.toString(lineIndex);
        }

        if (StringUtils.isNotBlank(collectionGroup.getSubCollectionSuffix())) {
            lineSuffix = collectionGroup.getSubCollectionSuffix() + lineSuffix;
        }

        // check for remote fields holder
        List<Field> lineFields = processAnyRemoteFieldsHolder(view, model, collectionGroup, lineItems);

        // copy fields for line and adjust binding to match collection line path
        lineFields = (List<Field>) ComponentUtils.copyFieldList(lineFields, bindingPath, lineSuffix);

        //If the fields contain any collections themselves (details case) adjust their binding path
        for(Field f: lineFields){
            List<CollectionGroup> components = ComponentUtils.getComponentsOfTypeDeep(f, CollectionGroup.class);
            for(CollectionGroup cg: components){
                ComponentUtils.prefixBindingPath(cg,bindingPath);
                cg.setSubCollectionSuffix(lineSuffix);
            }
        }


        boolean readOnlyLine = collectionGroup.isReadOnly();

        // update contexts before add line fields are added to the index below
        ComponentUtils.updateContextsForLine(lineFields, currentLine, lineIndex);

        // add special css styles to identify the add line client side
        if (lineIndex == -1) {

            // set focus on after the add line submit to first field of add line
            for (Action action : actions) {
                if (action.getActionParameter(UifParameters.ACTION_TYPE).equals(UifParameters.ADD_LINE) && (lineFields
                        .size() > 0)) {
                    action.setFocusOnIdAfterSubmit(lineFields.get(0).getId() + UifConstants.IdSuffixes.CONTROL);
                }
            }
        } else {
            // for existing lines, check view line auth
            boolean canViewLine = checkViewLineAuthorizationAndPresentationLogic(view, (ViewModel) model,
                    collectionGroup, currentLine);

            // if line is not viewable, just return without calling the layout manager to add the line
            if (!canViewLine) {
                return;
            }

            // check edit line authorization if collection is not read only
            if (!collectionGroup.isReadOnly()) {
                readOnlyLine = !checkEditLineAuthorizationAndPresentationLogic(view, (ViewModel) model, collectionGroup,
                        currentLine);

                // Add script to fields to activate save button on any change
                if (!((UifFormBase) model).isAddedCollectionItem(currentLine) &&
                        collectionGroup.isRenderSaveLineActions()) {
                    for (Field f : lineFields) {
                        if (f instanceof InputField && f.isRender()) {
                            ControlBase control = (ControlBase)((InputField) f).getControl();
                            control.setOnChangeScript(control.getOnChangeScript()==null?"":control.getOnChangeScript() +
                                    ";collectionLineChanged(this, 'uif-newCollectionItem');");
                        }
                    }
                }

                // Add script to recalculate totals
                // TODO : only add to total column fields, and add the add line
                for (Field f : lineFields) {
                    if (f instanceof InputField && f.isRender()) {
                        ControlBase control = (ControlBase)((InputField) f).getControl();
                        control.setOnChangeScript(control.getOnChangeScript()==null?"":control.getOnChangeScript() +
                                ";refreshDatatableCellRedraw(this);");
                    }
                }
            }

            ComponentUtils.pushObjectToContext(lineFields, UifConstants.ContextVariableNames.READONLY_LINE,
                    readOnlyLine);
            ComponentUtils.pushObjectToContext(actions, UifConstants.ContextVariableNames.READONLY_LINE, readOnlyLine);
        }

        // check authorization for line fields
        applyLineFieldAuthorizationAndPresentationLogic(view, (ViewModel) model, collectionGroup, currentLine,
                readOnlyLine, lineFields, actions);

        if (bindToForm) {
            ComponentUtils.setComponentsPropertyDeep(lineFields, UifPropertyPaths.BIND_TO_FORM, Boolean.valueOf(true));
        }

        // remove fields from the line that have render false
        lineFields = removeNonRenderLineFields(view, model, collectionGroup, lineFields, currentLine, lineIndex);

        // if not add line build sub-collection field groups
        List<FieldGroup> subCollectionFields = new ArrayList<FieldGroup>();
        if ((lineIndex != -1) && (collectionGroup.getSubCollections() != null)) {
            for (int subLineIndex = 0; subLineIndex < collectionGroup.getSubCollections().size(); subLineIndex++) {
                CollectionGroup subCollectionPrototype = collectionGroup.getSubCollections().get(subLineIndex);
                CollectionGroup subCollectionGroup = ComponentUtils.copy(subCollectionPrototype, lineSuffix);

                // verify the sub-collection should be rendered
                boolean renderSubCollection = checkSubCollectionRender(view, model, collectionGroup,
                        subCollectionGroup);
                if (!renderSubCollection) {
                    continue;
                }

                subCollectionGroup.getBindingInfo().setBindByNamePrefix(bindingPath);
                if (subCollectionGroup.isRenderAddLine()) {
                    subCollectionGroup.getAddLineBindingInfo().setBindByNamePrefix(bindingPath);
                }

                // set sub-collection suffix on group so it can be used for generated groups
                String subCollectionSuffix = lineSuffix;
                if (StringUtils.isNotBlank(subCollectionGroup.getSubCollectionSuffix())) {
                    subCollectionSuffix = subCollectionGroup.getSubCollectionSuffix() + lineSuffix;
                }
                subCollectionGroup.setSubCollectionSuffix(subCollectionSuffix);

                FieldGroup fieldGroupPrototype = layoutManager.getSubCollectionFieldGroupPrototype();

                FieldGroup subCollectionFieldGroup = ComponentUtils.copy(fieldGroupPrototype,
                        lineSuffix + UifConstants.IdSuffixes.SUB + subLineIndex);
                subCollectionFieldGroup.setGroup(subCollectionGroup);

                ComponentUtils.updateContextForLine(subCollectionFieldGroup, currentLine, lineIndex);

                subCollectionFields.add(subCollectionFieldGroup);
            }
            ComponentUtils.pushObjectToContext(subCollectionFields, UifConstants.ContextVariableNames.PARENT_LINE,
                    currentLine);
        }

        // invoke layout manager to build the complete line
        layoutManager.buildLine(view, model, collectionGroup, lineFields, subCollectionFields, bindingPath, actions,
                lineSuffix, currentLine, lineIndex);

        //add additional information to the group and fields to allow for correct add control selection
        String selector = "";
        if (lineIndex == -1) {
            List<String> addIds = new ArrayList<String>();
            for (Field f : lineFields) {
                if (f instanceof InputField) {
                    // sets up - skipping these fields in add area during standard form validation calls
                    // custom addLineToCollection js call will validate these fields manually on an add
                    Control control = ((InputField) f).getControl();
                    if (control != null) {
                        control.addStyleClass("ignoreValid");
                        selector = selector + ",#" + f.getId() + UifConstants.IdSuffixes.CONTROL;
                    }
                } else if (f instanceof FieldGroup) {
                    List<InputField> fields = ComponentUtils.getComponentsOfTypeDeep(((FieldGroup) f).getGroup(),
                            InputField.class);
                    for (InputField nestedField : fields) {
                        Control control = nestedField.getControl();
                        if (control != null) {
                            control.addStyleClass("ignoreValid");
                            selector = selector + ",#" + nestedField.getId() + UifConstants.IdSuffixes.CONTROL;
                        }
                    }
                }
            }
            collectionGroup.addDataAttribute("addControls", selector.replaceFirst(",", ""));
        }
    }

    /**
     * Iterates through the given items checking for {@code RemotableFieldsHolder}, if found
     * the holder is invoked to retrieved the remotable fields and translate to attribute fields. The translated list
     * is then inserted into the returned list at the position of the holder
     *
     * @param view - view instance containing the container
     * @param model - object instance containing the view data
     * @param group - collection group instance to check for any remotable fields holder
     * @param items - list of items to process
     */
    protected List<Field> processAnyRemoteFieldsHolder(View view, Object model, CollectionGroup group,
            List<? extends Component> items) {
        List<Field> processedItems = new ArrayList<Field>();

        // check for holders and invoke to retrieve the remotable fields and translate
        // translated fields are placed into the processed items list at the position of the holder
        for (Component item : items) {
            if (item instanceof RemoteFieldsHolder) {
                List<InputField> translatedFields = ((RemoteFieldsHolder) item).fetchAndTranslateRemoteFields(view,
                        model, group);
                processedItems.addAll(translatedFields);
            } else {
                processedItems.add((Field) item);
            }
        }

        return processedItems;
    }

    /**
     * Evaluates the render property for the given list of {@code Field}
     * instances for the line and removes any fields from the returned list that
     * have render false. The conditional render string is also taken into
     * account. This needs to be done here as opposed to during the normal
     * condition evaluation so the the fields are not used while building the
     * collection lines
     *
     * @param view - view instance the collection group belongs to
     * @param model - object containing the view data
     * @param collectionGroup - collection group for the line fields
     * @param lineFields - list of fields configured for the line
     * @param currentLine - object containing the line data
     * @param lineIndex - index of the line in the collection
     * @return List<Field> list of field instances that should be rendered
     */
    protected List<Field> removeNonRenderLineFields(View view, Object model, CollectionGroup collectionGroup,
            List<Field> lineFields, Object currentLine, int lineIndex) {
        List<Field> fields = new ArrayList<Field>();

        for (Field lineField : lineFields) {
            String conditionalRender = lineField.getPropertyExpression("render");

            // evaluate conditional render string if set
            if (StringUtils.isNotBlank(conditionalRender)) {
                Map<String, Object> context = getContextForField(view, collectionGroup, lineField);

                // Adjust the condition as ExpressionUtils.adjustPropertyExpressions will only be
                // executed after the collection is built.
                conditionalRender = KRADServiceLocatorWeb.getExpressionEvaluatorService().replaceBindingPrefixes(view,
                        lineField, conditionalRender);

                Boolean render = (Boolean) getExpressionEvaluatorService().evaluateExpression(model, context,
                        conditionalRender);
                lineField.setRender(render);
            }

            // only add line field if set to render or if it is hidden by progressive render
            if (lineField.isRender() || StringUtils.isNotBlank(lineField.getProgressiveRender())) {
                fields.add(lineField);
            }
        }

        return fields;
    }

    /**
     * Invokes the view's configured authorizer and presentation controller to determine if the user has permission
     * to view the line (if a permission has been established)
     *
     * @param view - view instance the collection belongs to and from which the authorizer/presentation controller will
     * be pulled
     * @param model - object containing the view's data
     * @param collectionGroup - collection group containing the line
     * @param line - object containing the lines data
     * @return boolean true if the user can view the line, false if not
     */
    protected boolean checkViewLineAuthorizationAndPresentationLogic(View view, ViewModel model,
            CollectionGroup collectionGroup, Object line) {
        ViewPresentationController presentationController = view.getPresentationController();
        ViewAuthorizer authorizer = view.getAuthorizer();

        Person user = GlobalVariables.getUserSession().getPerson();

        // check view line auth
        boolean canViewLine = authorizer.canViewLine(view, model, collectionGroup, collectionGroup.getPropertyName(),
                line, user);
        if (canViewLine) {
            canViewLine = presentationController.canViewLine(view, model, collectionGroup,
                    collectionGroup.getPropertyName(), line);
        }

        return canViewLine;
    }

    /**
     * Invokes the view's configured authorizer and presentation controller to determine if the user has permission
     * to edit the line (if a permission has been established)
     *
     * @param view - view instance the collection belongs to and from which the authorizer/presentation controller will
     * be pulled
     * @param model - object containing the view's data
     * @param collectionGroup - collection group containing the line
     * @param line - object containing the lines data
     * @return boolean true if the user can edit the line, false if not
     */
    protected boolean checkEditLineAuthorizationAndPresentationLogic(View view, ViewModel model,
            CollectionGroup collectionGroup, Object line) {
        ViewPresentationController presentationController = view.getPresentationController();
        ViewAuthorizer authorizer = view.getAuthorizer();

        Person user = GlobalVariables.getUserSession().getPerson();

        // check edit line auth
        boolean canEditLine = authorizer.canEditLine(view, model, collectionGroup, collectionGroup.getPropertyName(),
                line, user);
        if (canEditLine) {
            canEditLine = presentationController.canEditLine(view, model, collectionGroup,
                    collectionGroup.getPropertyName(), line);
        }

        return canEditLine;
    }

    /**
     * Iterates through the line fields and checks the view field authorization using the view's configured authorizer
     * and presentation controller. If the field is viewable, then sets the edit field authorization. Finally iterates
     * through the line actions invoking the authorizer and presentation controller to authorizer the action
     *
     * @param view - view instance the collection belongs to and from which the authorizer/presentation controller will
     * be pulled
     * @param model - object containing the view's data
     * @param collectionGroup - collection group containing the line
     * @param line - object containing the lines data
     * @param readOnlyLine - flag indicating whether the line has been marked as read only (which will force the fields
     * to be read only)
     * @param lineFields - list of fields instances for the line
     * @param actions - list of action field instances for the line
     */
    protected void applyLineFieldAuthorizationAndPresentationLogic(View view, ViewModel model,
            CollectionGroup collectionGroup, Object line, boolean readOnlyLine, List<Field> lineFields,
            List<Action> actions) {
        ViewPresentationController presentationController = view.getPresentationController();
        ViewAuthorizer authorizer = view.getAuthorizer();

        Person user = GlobalVariables.getUserSession().getPerson();

        for (Field lineField : lineFields) {
            String propertyName = null;
            if (lineField instanceof DataBinding) {
                propertyName = ((DataBinding) lineField).getPropertyName();
            }

            // evaluate expression on fields component security (since apply model phase has not been invoked on
            // them yet
            ComponentSecurity componentSecurity = lineField.getComponentSecurity();

            Map<String, Object> context = getContextForField(view, collectionGroup, lineField);
            getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(view, componentSecurity, model, context);

            // check view field auth
            if (lineField.isRender() && !lineField.isHidden()) {
                boolean canViewField = authorizer.canViewLineField(view, model, collectionGroup,
                        collectionGroup.getPropertyName(), line, lineField, propertyName, user);
                if (canViewField) {
                    canViewField = presentationController.canViewLineField(view, model, collectionGroup,
                            collectionGroup.getPropertyName(), line, lineField, propertyName);
                }

                if (!canViewField) {
                    // since removing can impact layout, set to hidden
                    // TODO: check into encryption setting
                    lineField.setHidden(true);

                    if (lineField.getPropertyExpressions().containsKey("hidden")) {
                        lineField.getPropertyExpressions().remove("hidden");
                    }

                    continue;
                }

                // check edit field auth
                boolean canEditField = !readOnlyLine;
                if (!readOnlyLine) {
                    canEditField = authorizer.canEditLineField(view, model, collectionGroup,
                            collectionGroup.getPropertyName(), line, lineField, propertyName, user);
                    if (canEditField) {
                        canEditField = presentationController.canEditLineField(view, model, collectionGroup,
                                collectionGroup.getPropertyName(), line, lineField, propertyName);
                    }
                }

                if (readOnlyLine || !canEditField) {
                    lineField.setReadOnly(true);

                    if (lineField.getPropertyExpressions().containsKey("readOnly")) {
                        lineField.getPropertyExpressions().remove("readOnly");
                    }
                }
            }
        }

        // check auth on line actions
        for (Action action : actions) {
            if (action.isRender()) {
                boolean canPerformAction = authorizer.canPerformLineAction(view, model, collectionGroup,
                        collectionGroup.getPropertyName(), line, action, action.getActionEvent(), action.getId(), user);
                if (canPerformAction) {
                    canPerformAction = presentationController.canPerformLineAction(view, model, collectionGroup,
                            collectionGroup.getPropertyName(), line, action, action.getActionEvent(), action.getId());
                }

                if (!canPerformAction) {
                    action.setRender(false);

                    if (action.getPropertyExpressions().containsKey("render")) {
                        action.getPropertyExpressions().remove("render");
                    }
                }
            }
        }
    }

    /**
     * Checks whether the given sub-collection should be rendered, any
     * conditional render string is evaluated
     *
     * @param view - view instance the sub collection belongs to
     * @param model - object containing the view data
     * @param collectionGroup - collection group the sub collection belongs to
     * @param subCollectionGroup - sub collection group to check render status for
     * @return boolean true if sub collection should be rendered, false if it
     *         should not be rendered
     */
    protected boolean checkSubCollectionRender(View view, Object model, CollectionGroup collectionGroup,
            CollectionGroup subCollectionGroup) {
        String conditionalRender = subCollectionGroup.getPropertyExpression("render");

        // TODO: check authorizer

        // evaluate conditional render string if set
        if (StringUtils.isNotBlank(conditionalRender)) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.putAll(view.getContext());
            context.put(UifConstants.ContextVariableNames.PARENT, collectionGroup);
            context.put(UifConstants.ContextVariableNames.COMPONENT, subCollectionGroup);

            Boolean render = (Boolean) getExpressionEvaluatorService().evaluateExpression(model, context,
                    conditionalRender);
            subCollectionGroup.setRender(render);
        }

        return subCollectionGroup.isRender();
    }

    /**
     * Creates new {@code Action} instances for the line
     *
     * <p>
     * Adds context to the action fields for the given line so that the line the
     * action was performed on can be determined when that action is selected
     * </p>
     *
     * @param lineActions - the actions to copy
     * @param view - view instance the collection belongs to
     * @param model - top level object containing the data
     * @param collectionGroup - collection group component for the collection
     * @param collectionLine - object instance for the current line
     * @param lineIndex - index of the line the actions should apply to
     */
    protected List<Action> initializeLineActions(List<Action> lineActions, View view, Object model,
            CollectionGroup collectionGroup, Object collectionLine, int lineIndex, String actionScript) {
        String lineSuffix = UifConstants.IdSuffixes.LINE + Integer.toString(lineIndex);
        if (StringUtils.isNotBlank(collectionGroup.getSubCollectionSuffix())) {
            lineSuffix = collectionGroup.getSubCollectionSuffix() + lineSuffix;
        }
        List<Action> actions = ComponentUtils.copyComponentList(lineActions, lineSuffix);

        for (Action action : actions) {
            action.addActionParameter(UifParameters.SELLECTED_COLLECTION_PATH,
                    collectionGroup.getBindingInfo().getBindingPath());
            action.addActionParameter(UifParameters.SELECTED_LINE_INDEX, Integer.toString(lineIndex));
            action.setJumpToIdAfterSubmit(collectionGroup.getId());

            if (StringUtils.isNotBlank(action.getActionScript())) {
                actionScript = action.getActionScript() + actionScript;
            }
            action.setActionScript(actionScript);
        }

        ComponentUtils.updateContextsForLine(actions, collectionLine, lineIndex);

        return actions;
    }

    /**
     * Creates new {@code Action} instances for the add line
     *
     * <p>
     * Adds context to the action fields for the add line so that the collection
     * the action was performed on can be determined
     * </p>
     *
     * @param view - view instance the collection belongs to
     * @param model - top level object containing the data
     * @param collectionGroup - collection group component for the collection
     */
    protected List<Action> getAddLineActions(View view, Object model, CollectionGroup collectionGroup) {
        String lineSuffix = UifConstants.IdSuffixes.ADD_LINE;
        if (StringUtils.isNotBlank(collectionGroup.getSubCollectionSuffix())) {
            lineSuffix = collectionGroup.getSubCollectionSuffix() + lineSuffix;
        }
        List<Action> lineActions = ComponentUtils.copyComponentList(collectionGroup.getAddLineActions(), lineSuffix);

        for (Action action : lineActions) {
            action.addActionParameter(UifParameters.SELLECTED_COLLECTION_PATH,
                    collectionGroup.getBindingInfo().getBindingPath());
            action.setJumpToIdAfterSubmit(collectionGroup.getId());
            action.addActionParameter(UifParameters.ACTION_TYPE, UifParameters.ADD_LINE);

            String baseId = collectionGroup.getBaseId();
            if (StringUtils.isNotBlank(collectionGroup.getSubCollectionSuffix())) {
                baseId += collectionGroup.getSubCollectionSuffix();
            }

            String actionScript = "addLineToCollection(this, '" + collectionGroup.getId() + "', '" + baseId + "')";
            if (collectionGroup.isAddViaLightBox()) {
                actionScript = "if (" + actionScript + ") {closeLightbox();}";
            }
            action.setActionScript(actionScript + ";");

        }

        // get add line for context
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object addLine = ObjectPropertyUtils.getPropertyValue(model, addLinePath);

        ComponentUtils.updateContextsForLine(lineActions, addLine, -1);

        return lineActions;
    }

    /**
     * Helper method to build the context for a field (needed because the apply model phase for line fields has
     * not been applied yet and their full context not set)
     *
     * @param view - view instance the field belongs to
     * @param collectionGroup - collection group instance the field belongs to
     * @param field - field instance to build context for
     * @return Map<String, Object> context for field
     */
    protected Map<String, Object> getContextForField(View view, CollectionGroup collectionGroup, Field field) {
        Map<String, Object> context = new HashMap<String, Object>();

        context.putAll(view.getContext());
        context.putAll(field.getContext());
        context.put(UifConstants.ContextVariableNames.PARENT, collectionGroup);
        context.put(UifConstants.ContextVariableNames.COMPONENT, field);

        return context;
    }

    /**
     * Initializes a new instance of the collection class
     *
     * <p>
     * If the add line property was not specified for the collection group the
     * new lines will be added to the generic map on the
     * {@code UifFormBase}, else it will be added to the property given by
     * the addLineBindingInfo
     * </p>
     *
     * <p>
     * New line will only be created if the current line property is null or
     * clearExistingLine is true. In the case of a new line default values are
     * also applied
     * </p>
     *
     * @see CollectionGroup#
     *      initializeNewCollectionLine(View, Object, CollectionGroup, boolean)
     */
    public void initializeNewCollectionLine(View view, Object model, CollectionGroup collectionGroup,
            boolean clearExistingLine) {
        Object newLine = null;

        // determine if we are binding to generic form map or a custom property
        if (StringUtils.isBlank(collectionGroup.getAddLinePropertyName())) {
            // bind to form map
            if (!(model instanceof UifFormBase)) {
                throw new RuntimeException("Cannot create new collection line for group: "
                        + collectionGroup.getPropertyName()
                        + ". Model does not extend "
                        + UifFormBase.class.getName());
            }

            // get new collection line map from form
            Map<String, Object> newCollectionLines = ObjectPropertyUtils.getPropertyValue(model,
                    UifPropertyPaths.NEW_COLLECTION_LINES);
            if (newCollectionLines == null) {
                newCollectionLines = new HashMap<String, Object>();
                ObjectPropertyUtils.setPropertyValue(model, UifPropertyPaths.NEW_COLLECTION_LINES, newCollectionLines);
            }

            // set binding path for add line
            String newCollectionLineKey = KRADUtils.translateToMapSafeKey(
                    collectionGroup.getBindingInfo().getBindingPath());
            String addLineBindingPath = UifPropertyPaths.NEW_COLLECTION_LINES + "['" + newCollectionLineKey + "']";
            collectionGroup.getAddLineBindingInfo().setBindingPath(addLineBindingPath);

            // if there is not an instance available or we need to clear create a new instance
            if (!newCollectionLines.containsKey(newCollectionLineKey) || (newCollectionLines.get(newCollectionLineKey)
                    == null) || clearExistingLine) {
                // create new instance of the collection type for the add line
                newLine = ObjectUtils.newInstance(collectionGroup.getCollectionObjectClass());
                newCollectionLines.put(newCollectionLineKey, newLine);
            }
        } else {
            // bind to custom property
            Object addLine = ObjectPropertyUtils.getPropertyValue(model,
                    collectionGroup.getAddLineBindingInfo().getBindingPath());
            if ((addLine == null) || clearExistingLine) {
                newLine = ObjectUtils.newInstance(collectionGroup.getCollectionObjectClass());
                ObjectPropertyUtils.setPropertyValue(model, collectionGroup.getAddLineBindingInfo().getBindingPath(),
                        newLine);
            }
        }

        // apply default values if a new line was created
        if (newLine != null) {
            view.getViewHelperService().applyDefaultValuesForCollectionLine(view, model, collectionGroup, newLine);
        }
    }

    protected ExpressionEvaluatorService getExpressionEvaluatorService() {
        return KRADServiceLocatorWeb.getExpressionEvaluatorService();
    }

}
