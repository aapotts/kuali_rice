/**
 * Copyright 2005-2013 The Kuali Foundation
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

import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.component.ComponentBase;
import org.kuali.rice.krad.uif.field.FieldBase;
import org.kuali.rice.krad.uif.field.InputField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * ComponentUtilsTest tests various ComponentUtils methods
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ComponentUtilsTest {

    private String componentId;
    private Component component;

    @Before
    public void setup() {
        component = new InputField();
        componentId = "field1";
        component.setId(componentId);
        component.setBaseId(componentId);
    }


    // Initialization methods
    private FieldBase initializeFieldBase() {
        FieldBase fieldBase = new FieldBase();
        fieldBase = (FieldBase) initializeComponentBase(fieldBase);

        return fieldBase;
    }

    private ComponentBase initializeComponentBase(ComponentBase componentBase) {
        List<String> additionalComponentsToRefresh = new ArrayList<String>();
        additionalComponentsToRefresh.add("A");
        additionalComponentsToRefresh.add("B");
        additionalComponentsToRefresh.add("C");
        componentBase.setAdditionalComponentsToRefresh(additionalComponentsToRefresh);

        List<String> additionalCssClasses = new ArrayList<String>();
        additionalCssClasses.add("Class1");
        additionalCssClasses.add("Class2");
        additionalCssClasses.add("Class3");
        componentBase.setAdditionalCssClasses(additionalCssClasses);

        componentBase.setAlign("right");

        List<String> cellCssClasses = new ArrayList<String>();
        cellCssClasses.add("CellClass1");
        cellCssClasses.add("CellClass2");
        cellCssClasses.add("CellClass3");
        componentBase.setCellCssClasses(cellCssClasses);

        componentBase.setCellStyle("Style1");
        componentBase.setCellWidth("20px");
        componentBase.setColSpan(2);
        componentBase.setConditionalRefresh("Refresh");

        List<String> cssClasses = new ArrayList<String>();
        cssClasses.add("CssClass1");
        cssClasses.add("CssClass2");
        cssClasses.add("CssClass3");
        componentBase.setCssClasses(cssClasses);

        Map<String, String> dataAttributes = new HashMap<String, String>();
        dataAttributes.put("One", "A");
        dataAttributes.put("Two", "B");
        dataAttributes.put("Three", "C");
        componentBase.setDataAttributes(dataAttributes);

        componentBase.setFinalizeMethodToCall("methodA");
        componentBase.setMethodToCallOnRefresh("methodB");
        componentBase.setOnBlurScript("onblurscript");
        componentBase.setOnChangeScript("onchangescript");
        componentBase.setOnClickScript("onclickscript");
        componentBase.setOnCloseScript("onclosescript");
        componentBase.setOnDblClickScript("ondblclickscript");
        componentBase.setOnDocumentReadyScript("ondocreadyscript");
        componentBase.setOnFocusScript("onfocusscript");
        componentBase.setOnKeyDownScript("onkeydownscript");
        componentBase.setOnKeyPressScript("onkeypressscript");
        componentBase.setOnKeyUpScript("onkeyupscript");
        componentBase.setOnLoadScript("onloadscript");
        componentBase.setOnMouseDownScript("onmousedownscript");
        componentBase.setOnMouseMoveScript("onmousemovescript");
        componentBase.setOnMouseOutScript("onmouseoutscript");
        componentBase.setOnMouseOverScript("onmouseoverscript");
        componentBase.setOnMouseUpScript("onmouseupscript");
        componentBase.setOnSubmitScript("onsubmitscript");
        componentBase.setOnUnloadScript("onunloadscript");
        componentBase.setOrder(5);
        componentBase.setPostRenderContent("PostRenderContent");
        componentBase.setPreRenderContent("PreRenderContent");
        componentBase.setProgressiveRender("ProgressiveRender");
        componentBase.setReadOnly(false);
        componentBase.setRefreshedByAction(false);
        componentBase.setRefreshTimer(12);

        List<String> refreshWhenChangedPropertyNames = new ArrayList<String>();
        refreshWhenChangedPropertyNames.add("property1");
        refreshWhenChangedPropertyNames.add("property2");
        refreshWhenChangedPropertyNames.add("property3");
        componentBase.setRefreshWhenChangedPropertyNames(refreshWhenChangedPropertyNames);

        componentBase.setRenderedHtmlOutput("<output>");
        componentBase.setRowSpan(3);
        componentBase.setStyle("slick");
        componentBase.setTemplate("TemplateA");
        componentBase.setTemplateName("TemplateName");

        Map<String, String> templateOptions = new HashMap<String, String>();
        templateOptions.put("Option1", "Value1");
        templateOptions.put("Option1", "Value2");
        templateOptions.put("Option1", "Value3");
        componentBase.setTemplateOptions(templateOptions);

        componentBase.setTemplateOptionsJSString("OptionsJS");
        componentBase.setTitle("Title");
        componentBase.setValign("middle");
        componentBase.setWidth("30px");

        return componentBase;
    }

    // End of Initialization methods

    @Test
    /**
     * test that {@link ComponentUtils#updateIdWithSuffix} works ok
     */
    public void testUpdateIdWithSuffix() {
        ComponentUtils.updateIdWithSuffix(component, null);
        assertTrue(component.getId().equalsIgnoreCase(componentId));

        String suffix = "_field";
        ComponentUtils.updateIdWithSuffix(component, suffix);
        assertTrue(component.getId().equalsIgnoreCase(componentId + suffix));

    }

    @Test
    /**
     * test {@link ComponentUtils#copyUsingCloning} using a FieldBase object
     */
    public void testCopyUsingCloningWithFieldBaseSucceeds() {
        FieldBase fieldBaseOriginal = initializeFieldBase();
        FieldBase fieldBaseCopy = ComponentUtils.copyUsingCloning(fieldBaseOriginal);

        assertTrue(ComponentCopyPropertiesMatch(fieldBaseOriginal, fieldBaseCopy));
    }

    private boolean ComponentCopyPropertiesMatch(ComponentBase originalComponent, ComponentBase copiedComponent) {
        boolean result = true;

        List<String> missingComponentsToRefresh = originalComponent.getAdditionalComponentsToRefresh();
        missingComponentsToRefresh.removeAll(copiedComponent.getAdditionalComponentsToRefresh());
        if (!missingComponentsToRefresh.isEmpty()) result = false;

        List<String> missingAdditionalCssClasses = originalComponent.getAdditionalCssClasses();
        missingAdditionalCssClasses.removeAll(copiedComponent.getAdditionalCssClasses());
        if (!missingAdditionalCssClasses.isEmpty()) result = false;

        if (!originalComponent.getAlign().equals(copiedComponent.getAlign())) result = false;

        List<String> missingCellCssClasses = originalComponent.getCellCssClasses();
        missingCellCssClasses.removeAll(copiedComponent.getCellCssClasses());
        if (!missingCellCssClasses.isEmpty()) result = false;

        if (!originalComponent.getCellStyle().equals(copiedComponent.getCellStyle())) result = false;
        if (!originalComponent.getCellWidth().equals(copiedComponent.getCellWidth())) result = false;
        if (originalComponent.getColSpan() != copiedComponent.getColSpan()) result = false;
        if (!originalComponent.getConditionalRefresh().equals(copiedComponent.getConditionalRefresh())) result = false;

        List<String> missingCssClasses = originalComponent.getCssClasses();
        missingCssClasses.removeAll(copiedComponent.getCssClasses());
        if (!missingCssClasses.isEmpty()) result = false;

        Set dataAttributes = new HashSet(originalComponent.getDataAttributes().values());
        dataAttributes.removeAll(copiedComponent.getDataAttributes().values());
        if (!dataAttributes.isEmpty()) result = false;

        if (!originalComponent.getFinalizeMethodToCall().equals(copiedComponent.getFinalizeMethodToCall())) result = false;
        if (!originalComponent.getMethodToCallOnRefresh().equals(copiedComponent.getMethodToCallOnRefresh())) result = false;
        if (!originalComponent.getOnBlurScript().equals(copiedComponent.getOnBlurScript())) result = false;
        if (!originalComponent.getOnChangeScript().equals(copiedComponent.getOnChangeScript())) result = false;
        if (!originalComponent.getOnClickScript().equals(copiedComponent.getOnClickScript())) result = false;
        if (!originalComponent.getOnCloseScript().equals(copiedComponent.getOnCloseScript())) result = false;
        if (!originalComponent.getOnDblClickScript().equals(copiedComponent.getOnDblClickScript())) result = false;
        if (!originalComponent.getOnDocumentReadyScript().equals(copiedComponent.getOnDocumentReadyScript())) result = false;
        if (!originalComponent.getOnFocusScript().equals(copiedComponent.getOnFocusScript())) result = false;
        if (!originalComponent.getOnKeyDownScript().equals(copiedComponent.getOnKeyDownScript())) result = false;
        if (!originalComponent.getOnKeyPressScript().equals(copiedComponent.getOnKeyPressScript())) result = false;
        if (!originalComponent.getOnKeyUpScript().equals(copiedComponent.getOnKeyUpScript())) result = false;
        if (!originalComponent.getOnLoadScript().equals(copiedComponent.getOnLoadScript())) result = false;
        if (!originalComponent.getOnMouseDownScript().equals(copiedComponent.getOnMouseDownScript())) result = false;
        if (!originalComponent.getOnMouseMoveScript().equals(copiedComponent.getOnMouseMoveScript())) result = false;
        if (!originalComponent.getOnMouseOutScript().equals(copiedComponent.getOnMouseOutScript())) result = false;
        if (!originalComponent.getOnMouseOverScript().equals(copiedComponent.getOnMouseOverScript())) result = false;
        if (!originalComponent.getOnMouseUpScript().equals(copiedComponent.getOnMouseUpScript())) result = false;
        if (!originalComponent.getOnSubmitScript().equals(copiedComponent.getOnSubmitScript())) result = false;
        if (!originalComponent.getOnUnloadScript().equals(copiedComponent.getOnUnloadScript())) result = false;
        if (originalComponent.getOrder() != copiedComponent.getOrder()) result = false;
        if (!originalComponent.getPostRenderContent().equals(copiedComponent.getPostRenderContent())) result = false;
        if (!originalComponent.getPreRenderContent().equals(copiedComponent.getPreRenderContent())) result = false;
        if (!originalComponent.getProgressiveRender().equals(copiedComponent.getProgressiveRender())) result = false;
        if (originalComponent.getRequired() != copiedComponent.getRequired()) result = false;
        if (originalComponent.getRefreshTimer() != copiedComponent.getRefreshTimer()) result = false;

        List<String> missingRefreshWhenChangedPropertyNames = originalComponent.getRefreshWhenChangedPropertyNames();
        missingRefreshWhenChangedPropertyNames.removeAll(copiedComponent.getRefreshWhenChangedPropertyNames());
        if (!missingRefreshWhenChangedPropertyNames.isEmpty()) result = false;

        if (!originalComponent.getRenderedHtmlOutput().equals(copiedComponent.getRenderedHtmlOutput())) result = false;
        if (originalComponent.getRowSpan() != copiedComponent.getRowSpan()) result = false;
        if (!originalComponent.getStyle().equals(copiedComponent.getStyle())) result = false;
        if (!originalComponent.getTemplate().equals(copiedComponent.getTemplate())) result = false;
        if (!originalComponent.getTemplateName().equals(copiedComponent.getTemplateName())) result = false;

        Set templateOptions = new HashSet(originalComponent.getTemplateOptions().values());
        templateOptions.removeAll(copiedComponent.getTemplateOptions().values());
        if (!templateOptions.isEmpty()) result = false;

        if (!originalComponent.getTemplateOptionsJSString().equals(copiedComponent.getTemplateOptionsJSString())) result = false;
        if (!originalComponent.getTitle().equals(copiedComponent.getTitle())) result = false;
        if (!originalComponent.getValign().equals(copiedComponent.getValign())) result = false;
        if (!originalComponent.getWidth().equals(copiedComponent.getWidth())) result = false;

        return result;
    }
}
