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
import org.kuali.rice.core.api.util.type.TypeUtils;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.component.DataBinding;
import org.kuali.rice.krad.uif.component.Ordered;
import org.kuali.rice.krad.uif.container.ContainerBase;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.layout.LayoutManager;
import org.kuali.rice.krad.util.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.OrderComparator;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ComponentUtils is a utility class providing methods to help create and modify <code>Component</code> instances
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ComponentUtils {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ComponentUtils.class);


    public static <T extends Component> T copy(T component) {
        return copy(component, null);
    }

    public static <T extends Component> T copy(T component, String idSuffix) {
        T copy = copyObject(component);

        if (StringUtils.isNotBlank(idSuffix)) {
            updateIdsWithSuffixNested(copy, idSuffix);
        }

        return copy;
    }

    public static <T extends Object> T copyObject(T object) {
        if (object == null) {
            return null;
        }

        T copy = null;
        try {
            copy = CloneUtils.deepClone(object);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return copy;
    }

    protected static Object getCopyPropertyValue(Set<String> propertiesForReferenceCopy, String propertyName,
            Object propertyValue) {
        if (propertyValue == null) {
            return null;
        }

        Object copyValue = propertyValue;

        Class<?> valuePropertyType = propertyValue.getClass();
        if (propertiesForReferenceCopy.contains(propertyName) || TypeUtils.isSimpleType(valuePropertyType)
                || TypeUtils.isClassClass(valuePropertyType)) {
            return copyValue;
        }

        if (Component.class.isAssignableFrom(valuePropertyType)
                || LayoutManager.class.isAssignableFrom(valuePropertyType)) {
            copyValue = copyObject(propertyValue);
        }
        else {
            copyValue = ObjectUtils.deepCopy((Serializable) propertyValue);
        }

        return copyValue;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Object> T getNewInstance(T object) {
        T copy = null;
        try {
            copy = (T) object.getClass().newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to create new instance of class: " + object.getClass());
        }

        return copy;
    }

    public static <T extends Field> List<T> copyFieldList(List<T> fields, String addBindingPrefix, String idSuffix) {
        List<T> copiedFieldList = copyFieldList(fields, idSuffix);

        prefixBindingPath(copiedFieldList, addBindingPrefix);

        return copiedFieldList;
    }

    public static <T extends Field> List<T> copyFieldList(List<T> fields, String idSuffix) {
        List<T> copiedFieldList = new ArrayList<T>();

        for (T field : fields) {
            T copiedField = copy(field, idSuffix);
            copiedFieldList.add(copiedField);
        }

        return copiedFieldList;
    }

    public static <T extends Component> T copyComponent(T component, String addBindingPrefix, String idSuffix) {
        T copy = copy(component, idSuffix);

        prefixBindingPathNested(copy, addBindingPrefix);

        return copy;
    }

    public static <T extends Component> List<T> copyComponentList(List<T> components, String idSuffix) {
        List<T> copiedComponentList = new ArrayList<T>();

        for (T field : components) {
            T copiedComponent = copy(field, idSuffix);
            copiedComponentList.add(copiedComponent);
        }

        return copiedComponentList;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Component> List<T> getComponentsOfType(List<? extends Component> items,
            Class<T> componentType) {
        List<T> typeComponents = new ArrayList<T>();

        for (Component component : items) {
            if (componentType.isAssignableFrom(component.getClass())) {
                typeComponents.add((T) component);
            }
        }

        return typeComponents;
    }

    /**
     * Return the components of the specified type from the given component list
     *
     * <p>
     * Components that match, implement or are extended from the specified {@code componentType} are returned in
     * the result.  If a component is a parent to other components then these child components are searched for
     * matching component types as well.
     * </p>
     *
     * @param items list of components from which to search
     * @param componentType the class or interface of the component type to return
     * @param <T> the type of the components that are returned
     * @return List of matching components
     */
    public static <T extends Component> List<T> getComponentsOfTypeDeep(List<? extends Component> items,
            Class<T> componentType) {
        List<T> typeComponents = new ArrayList<T>();

        for (Component component : items) {
            typeComponents.addAll(getComponentsOfTypeDeep(component, componentType));
        }

        return typeComponents;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Component> List<T> getComponentsOfTypeDeep(Component component, Class<T> componentType) {
        List<T> typeComponents = new ArrayList<T>();

        if (component == null) {
            return typeComponents;
        }

        if (componentType.isAssignableFrom(component.getClass())) {
            typeComponents.add((T) component);
        }

        for (Component nested : component.getComponentsForLifecycle()) {
            typeComponents.addAll(getComponentsOfTypeDeep(nested, componentType));
        }

        return typeComponents;
    }

    public static List<Component> getAllNestedComponents(Component component) {
        List<Component> components = new ArrayList<Component>();

        if (component == null) {
            return components;
        }

        for (Component nested : component.getComponentsForLifecycle()) {
            if (nested != null) {
                components.add(nested);
                components.addAll(getAllNestedComponents(nested));
            }
        }

        return components;
    }

    /**
     * Searches for the component with the given id within the given list of components
     *
     * @param components list of components to search through
     * @param componentId id for the component to find
     * @return Component component found in the list or null
     */
    public static Component findComponentInList(List<Component> components, String componentId) {
        Component foundComponent = null;

        for (Component component : components) {
            if (component != null && component.getId().equals(componentId)) {
                foundComponent = component;
                break;
            }
        }

        return foundComponent;
    }

    /**
     * Finds the child component of the given parent component that has the required id
     *
     * @param parent - parent component for component to find
     * @param nestedId - id of the component to find
     * @return Component instance for child (if found) or null
     */
    public static Component findNestedComponentById(Component parent, String nestedId) {
        Component childComponent = null;

        List<Component> children = getAllNestedComponents(parent);
        for (Component child : children) {
            if (StringUtils.equals(nestedId, child.getId())) {
                childComponent = child;
                break;
            }
        }

        return childComponent;
    }

    public static void prefixBindingPath(List<? extends Field> fields, String addBindingPrefix) {
        for (Field field : fields) {
            if (field instanceof DataBinding) {
                prefixBindingPath((DataBinding) field, addBindingPrefix);
            }
            else if ((field instanceof FieldGroup) && (((FieldGroup) field).getItems() != null) ) {
                List<Field> groupFields = getComponentsOfTypeDeep(((FieldGroup) field).getItems(), Field.class);
                prefixBindingPath(groupFields, addBindingPrefix);
            }
        }
    }

    public static void prefixBindingPathNested(Component component, String addBindingPrefix) {
        if (component instanceof DataBinding) {
            if (LOG.isDebugEnabled()) {
                LOG.info("setting nested binding prefix '"+ addBindingPrefix  +"' on " + component);
            }
            prefixBindingPath((DataBinding) component, addBindingPrefix);
        }

        for (Component nested : component.getComponentsForLifecycle()) {
           if (nested != null) {
              prefixBindingPathNested(nested, addBindingPrefix);
           }
        }
    }

    public static void prefixBindingPath(DataBinding field, String addBindingPrefix) {
        String bindingPrefix = addBindingPrefix;
        if (StringUtils.isNotBlank(field.getBindingInfo().getBindByNamePrefix())) {
            bindingPrefix += "." + field.getBindingInfo().getBindByNamePrefix();
        }
        field.getBindingInfo().setBindByNamePrefix(bindingPrefix);
    }

    public static void updateIdsWithSuffixNested(List<? extends Component> components, String idSuffix) {
        for (Component component : components) {
            updateIdsWithSuffixNested(component, idSuffix);
        }
    }

    public static void updateIdsWithSuffixNested(Component component, String idSuffix) {
        updateIdWithSuffix(component, idSuffix);

        updateChildIdsWithSuffixNested(component, idSuffix);
    }

    public static void updateChildIdsWithSuffixNested(Component component, String idSuffix) {
        if (Container.class.isAssignableFrom(component.getClass())) {
            LayoutManager layoutManager = ((Container) component).getLayoutManager();
            layoutManager.setId(layoutManager.getId() + idSuffix);
        }

        for (Component nested : component.getComponentsForLifecycle()) {
            if (nested != null) {
                updateIdsWithSuffixNested(nested, idSuffix);
            }
        }

        for (Component nested : component.getPropertyReplacerComponents()) {
            if (nested != null) {
                updateIdsWithSuffixNested(nested, idSuffix);
            }
        }
    }

    /**
     * add a suffix to the id
     *
     * @param component - the component instance whose id will be changed
     * @param idSuffix - the suffix to be appended
     */
    public static void updateIdWithSuffix(Component component, String idSuffix) {
        if (component != null && !StringUtils.isEmpty(idSuffix)) {
            component.setId(component.getId() + idSuffix);
        }
    }

    public static void setComponentsPropertyDeep(List<? extends Component> components, String propertyPath,
            Object propertyValue) {
        for (Component component : components) {
            setComponentPropertyDeep(component, propertyPath, propertyValue);
        }
    }

    public static void setComponentPropertyDeep(Component component, String propertyPath, Object propertyValue) {
        ObjectPropertyUtils.setPropertyValue(component, propertyPath, propertyValue, true);

        for (Component nested : component.getComponentsForLifecycle()) {
            if (nested != null) {
                setComponentPropertyDeep(nested, propertyPath, propertyValue);
            }
        }
    }

    public static List<String> getComponentPropertyNames(Class<? extends Component> componentClass) {
        List<String> componentProperties = new ArrayList<String>();

        PropertyDescriptor[] properties = BeanUtils.getPropertyDescriptors(componentClass);
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor descriptor = properties[i];
            if (descriptor.getReadMethod() != null) {
                componentProperties.add(descriptor.getName());
            }
        }

        return componentProperties;
    }

    /**
     * places a key, value pair in each context map of a list of components
     *
     * @param components - the list components
     * @param contextName - a value to be used as a key to retrieve the object
     * @param contextValue - the value to be placed in the context
     */
    public static void pushObjectToContext(List<? extends Component> components, String contextName, Object contextValue) {
        for (Component component : components) {
            pushObjectToContext(component, contextName, contextValue);
        }
    }

    /**
     * pushes object to a component's context so that it is available from {@link Component#getContext()}
     *
     * <p>The component's nested components that are available via {@code Component#getComponentsForLifecycle}
     * are also updated recursively</p>
     *
     * @param component - the component whose context is to be updated
     * @param contextName - a value to be used as a key to retrieve the object
     * @param contextValue - the value to be placed in the context
     */
    public static void pushObjectToContext(Component component, String contextName, Object contextValue) {
        if (component == null) {
            return;
        }

        component.pushObjectToContext(contextName, contextValue);

        // special container check so we pick up the layout manager
        if (Container.class.isAssignableFrom(component.getClass())) {
            LayoutManager layoutManager = ((Container) component).getLayoutManager();
            if (layoutManager != null) {
                // add to layout manager context only if not present
                if (layoutManager.getContext().get(contextName) != contextValue) {
                    layoutManager.pushObjectToContext(contextName, contextValue);

                    for (Component nestedComponent : layoutManager.getComponentsForLifecycle()) {
                        pushObjectToContext(nestedComponent, contextName, contextValue);
                    }
                }
            }
        }

        for (Component nestedComponent : component.getComponentsForLifecycle()) {
            pushObjectToContext(nestedComponent, contextName, contextValue);
        }
    }

    /**
     * update the contexts of the given components
     *
     * <p>calls {@link #updateContextForLine(org.kuali.rice.krad.uif.component.Component, Object, int, String)}
     * for each component</p>
     *
     * @param components - the components whose components to update
     * @param collectionLine - an instance of the data object for the line
     * @param lineIndex - the line index
     * @param lineSuffix id suffix for components in the line to make them unique
     */
    public static void updateContextsForLine(List<? extends Component> components, Object collectionLine,
            int lineIndex, String lineSuffix) {
        for (Component component : components) {
            updateContextForLine(component, collectionLine, lineIndex, lineSuffix);
        }
    }

    /**
     * update the context map for the given component
     *
     * <p>The values of {@code UifConstants.ContextVariableNames.LINE} and {@code UifConstants.ContextVariableNames.INDEX}
     * are set to {@code collectionLine} and {@code lineIndex} respectively.</p>
     *
     * @param component - the component whose context is to be updated
     * @param collectionLine - an instance of the data object for the line
     * @param lineIndex - the line index
     * @param lineSuffix id suffix for components in the line to make them unique
     */
    public static void updateContextForLine(Component component, Object collectionLine, int lineIndex,
            String lineSuffix) {
        pushObjectToContext(component, UifConstants.ContextVariableNames.LINE, collectionLine);
        pushObjectToContext(component, UifConstants.ContextVariableNames.INDEX, Integer.valueOf(lineIndex));
        pushObjectToContext(component, UifConstants.ContextVariableNames.LINE_SUFFIX, lineSuffix);

        boolean isAddLine = (lineIndex == -1);
        pushObjectToContext(component, UifConstants.ContextVariableNames.IS_ADD_LINE, isAddLine);
    }

    /**
     * Performs sorting logic of the given list of <code>Ordered</code>
     * instances by its order property
     *
     * <p>
     * Items list is sorted based on its order property. Lower order values are
     * placed higher in the list. If a item does not have a value assigned for
     * the order (or is equal to the default order of 0), it will be assigned
     * the a value based on the given order sequence integer. If two or more
     * items share the same order value, all but the last item found in the list
     * will be removed.
     * </p>
     *
     * @param items
     * @param defaultOrderSequence
     * @return List<Ordered> sorted items
     * @see org.kuali.rice.krad.uif.component.Component#getOrder()
     * @see @see org.springframework.core.Ordered
     */
    public static List<? extends Ordered> sort(List<? extends Ordered> items, int defaultOrderSequence) {
        List<Ordered> orderedItems = new ArrayList<Ordered>();

        // do replacement for items with the same order property value
        Set<Integer> foundOrders = new HashSet<Integer>();

        // reverse the list, so items later in the list win
        Collections.reverse(items);
        for (Ordered component : items) {
            int order = component.getOrder();

            // if order not set just add to list
            if (order == 0) {
                orderedItems.add(component);
            }
            // check if the order value has been used already
            else if (!foundOrders.contains(Integer.valueOf(order))) {
                orderedItems.add(component);
                foundOrders.add(Integer.valueOf(order));
            }
        }

        // now reverse the list back so we can assign defaults for items without
        // an order value
        Collections.reverse(items);
        for (Ordered component : items) {
            int order = component.getOrder();

            // if order property not set assign default
            if (order == 0) {
                defaultOrderSequence++;
                while (foundOrders.contains(Integer.valueOf(defaultOrderSequence))) {
                    defaultOrderSequence++;
                }
                component.setOrder(defaultOrderSequence);
            }
        }

        // now sort the list by its order property
        Collections.sort(orderedItems, new OrderComparator());

        return orderedItems;
    }

    /**
     * Gets all the input fields contained in this container, but also in
     * every sub-container that is a child of this container.  When called from the top level
     * View this will be every InputField across all pages.
     *
     * @return every InputField that is a child at any level of this container
     */
    public static List<InputField> getAllInputFieldsWithinContainer(Container container) {
        List<InputField> inputFields = new ArrayList<InputField>();

        for (Component c : container.getComponentsForLifecycle()) {
            if (c instanceof InputField) {
                inputFields.add((InputField) c);
            } else if (c instanceof Container) {
                inputFields.addAll(getAllInputFieldsWithinContainer((Container) c));
            } else if (c instanceof FieldGroup) {
                Container cb = ((FieldGroup) c).getGroup();

                inputFields.addAll(getAllInputFieldsWithinContainer(cb));
            }
        }

        return inputFields;
    }

    /**
     * Determines whether the given component contains an expression for the given property name
     *
     * @param component component instance to check for expressions
     * @param propertyName name of the property to determine if there is an expression for
     * @param collectionMatch if set to true will find an expressions for properties that start with the given
     * property name (for matching expressions on collections like prop[index] or prop['key'])
     * @return boolean true if the component has an expression for the property name, false if not
     */
    public static boolean containsPropertyExpression(Component component, String propertyName,
            boolean collectionMatch) {
        boolean hasExpression = false;

        Map<String, String> propertyExpressions = component.getPropertyExpressions();

        if (collectionMatch) {
            for (String expressionPropertyName : propertyExpressions.keySet()) {
                if (expressionPropertyName.startsWith(propertyName)) {
                    hasExpression = true;
                }
            }
        } else if (propertyExpressions.containsKey(propertyName)) {
            hasExpression = true;
        }

        return hasExpression;
    }

}
