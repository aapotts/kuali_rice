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

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.krad.datadictionary.parse.BeanTag;
import org.kuali.rice.krad.datadictionary.parse.BeanTagAttribute;
import org.kuali.rice.krad.datadictionary.parse.BeanTags;
import org.kuali.rice.krad.datadictionary.uif.UifDictionaryBeanBase;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.PageGroup;
import org.kuali.rice.krad.uif.element.Header;
import org.kuali.rice.krad.uif.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ParentLocation is used to provide automatic generation/determination of Views/Pages that occur before the current
 * View.  Essentially, this class provides a way to determine a conceptual hierarchy of view/page locations.
 * This information is used internally to generate BreadcrumbItems that can appear before the View's breadcrumbs.
 */
@BeanTags({@BeanTag(name = "parentLocation-bean", parent = "Uif-ParentLocation"),
        @BeanTag(name = "ricePortalParentLocation", parent = "Uif-RicePortalParentLocation")})
public class ParentLocation extends UifDictionaryBeanBase implements Serializable {

    private static final long serialVersionUID = -6242148809697931126L;
    //private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ParentLocation.class);

    private UrlInfo parentViewUrl;
    private UrlInfo parentPageUrl;
    private String parentViewLabel;
    private String parentPageLabel;

    private BreadcrumbItem viewBreadcrumbItem;
    private BreadcrumbItem pageBreadcrumbItem;
    private List<BreadcrumbItem> resolvedBreadcrumbItems = new ArrayList<BreadcrumbItem>();

    /**
     * Construct the parent location breadcrumbItems that represent all the parent views/pages configured through
     * parentLocation by traversing through each view by id referenced in parentViewUrl in a chain recursively.  A url
     * which is not using viewId and instead set the href explicitly ends the chain.
     *
     * @param view the current view being processed
     * @param currentModel the currentModel
     * @param currentContext the currentContext
     * @return list of breadcrumbItems (the final list is set into the top most View's
     *         parentLocation.resolvedBreadcrumbItems)
     */
    public List<BreadcrumbItem> constructParentLocationBreadcrumbItems(View view, Object currentModel,
            Map<String, Object> currentContext) {
        //viewBreadcrumbItem must already have an object initialized
        if (viewBreadcrumbItem == null) {
            return resolvedBreadcrumbItems;
        }

        //evaluate expressions on relevant content before comparisons
        this.handleExpressions(view, currentModel, currentContext);

        //set url values into breadcrumb objects
        if (StringUtils.isNotBlank(parentViewUrl.getOriginalHref()) || (StringUtils.isNotBlank(
                parentViewUrl.getViewId()) && StringUtils.isNotBlank(parentViewUrl.getControllerMapping()))) {
            viewBreadcrumbItem.setUrl(parentViewUrl);
            viewBreadcrumbItem.setLabel(parentViewLabel);
        }

        if (StringUtils.isNotBlank(parentPageUrl.getOriginalHref()) || (StringUtils.isNotBlank(
                parentPageUrl.getViewId()) && StringUtils.isNotBlank(parentPageUrl.getControllerMapping()))) {
            pageBreadcrumbItem.setUrl(parentPageUrl);
            pageBreadcrumbItem.setLabel(parentPageLabel);
        }

        //only continue if either href or viewId are explicitly set (check for validity of parent url)
        if (viewBreadcrumbItem.getUrl() == null || StringUtils.isBlank(viewBreadcrumbItem.getUrl().getOriginalHref())
                && StringUtils.isBlank(viewBreadcrumbItem.getUrl().getViewId())) {
            return resolvedBreadcrumbItems;
        }

        String parentViewId = viewBreadcrumbItem.getUrl().getViewId();
        String controllerMapping = viewBreadcrumbItem.getUrl().getControllerMapping();

        View parentView = null;
        //chaining is only allowed when the controllerMapping and viewId are explicitly set
        if (viewBreadcrumbItem.getUrl() != null && StringUtils.isNotBlank(controllerMapping) && StringUtils.isNotBlank(
                parentViewId) && StringUtils.isBlank(viewBreadcrumbItem.getUrl().getOriginalHref())) {
            parentView = KRADServiceLocatorWeb.getDataDictionaryService().getViewById(parentViewId);
        }

        //only do this processing if the parentView is not null (viewId was set on viewBreadcrumbItem to a valid View)
        if (parentView != null) {
            processParentViewDerivedContent(parentView, parentViewId, view, currentModel, currentContext);
        }

        //add parent view breadcrumb
        if (StringUtils.isNotEmpty(viewBreadcrumbItem.getLabel())) {
            resolvedBreadcrumbItems.add(viewBreadcrumbItem);
        }

        //add parent page breadcrumb
        if (pageBreadcrumbItem != null && StringUtils.isNotEmpty(pageBreadcrumbItem.getLabel())) {
            resolvedBreadcrumbItems.add(pageBreadcrumbItem);
        }

        return resolvedBreadcrumbItems;
    }

    /**
     * Processes content that can only be derived by looking at the parentView for a parentLocation, such as expressions
     * and sibling breadcrumb content; evaluates and adds them to the ParentLocation BreadcrumbItem(s).
     *
     * @param parentView the parentView to derive breadcrumb content from
     * @param parentViewId the parentView's id
     * @param currentView the currentView (the view this parentLocation is on)
     * @param currentModel the current model data
     * @param currentContext the current context to evaluate expressions against
     */
    private void processParentViewDerivedContent(View parentView, String parentViewId, View currentView,
            Object currentModel, Map<String, Object> currentContext) {
        //populate expression graph
        ExpressionUtils.populatePropertyExpressionsFromGraph(parentView, false);

        //chain parent locations if not null on parent
        if (((View) parentView).getParentLocation() != null) {
            resolvedBreadcrumbItems.addAll(
                    ((View) parentView).getParentLocation().constructParentLocationBreadcrumbItems(parentView,
                            currentModel, currentContext));
        }

        handleLabelExpressions(parentView, currentModel, currentContext);

        //label automation, if parent has a label for its breadcrumb and one is not set here use that value
        //it is assumed that if the label contains a SpringEL expression, those properties are available on the
        //current form by the same name
        if (StringUtils.isBlank(viewBreadcrumbItem.getLabel()) && parentView.getBreadcrumbItem() != null &&
                StringUtils.isNotBlank(parentView.getBreadcrumbItem().getLabel())) {
            viewBreadcrumbItem.setLabel(parentView.getBreadcrumbItem().getLabel());
        } else if (StringUtils.isBlank(viewBreadcrumbItem.getLabel()) && StringUtils.isNotBlank(
                parentView.getHeaderText())) {
            viewBreadcrumbItem.setLabel(parentView.getHeaderText());
        }

        //siblingBreadcrumb inheritance automation
        if (parentView.getBreadcrumbItem() != null
                && parentView.getBreadcrumbItem().getSiblingBreadcrumbComponent() != null
                && viewBreadcrumbItem.getSiblingBreadcrumbComponent() == null) {
            currentView.assignComponentIds(parentView.getBreadcrumbItem().getSiblingBreadcrumbComponent());
            viewBreadcrumbItem.setSiblingBreadcrumbComponent(
                    parentView.getBreadcrumbItem().getSiblingBreadcrumbComponent());
        }

        //page breadcrumb label automation, page must be a page of the view breadcrumb
        if (pageBreadcrumbItem != null && StringUtils.isNotBlank(pageBreadcrumbItem.getUrl().getPageId()) && StringUtils
                .isNotBlank(pageBreadcrumbItem.getUrl().getViewId()) && pageBreadcrumbItem.getUrl().getViewId().equals(
                parentViewId)) {
            handlePageBreadcrumb(parentView, currentModel);
        }
    }

    /**
     * Evaluates the expressions on properties that may be determine the value of the label used on generated view and
     * page breadcrumbItems (if a label was not explicitly set)
     *
     * @param parentView the parentView
     * @param currentModel the currentModel
     * @param currentContext the currentContext
     */
    private void handleLabelExpressions(View parentView, Object currentModel, Map<String, Object> currentContext) {
        try {
            Header header = parentView.getHeader();

            if (header != null) {
                if (StringUtils.isNotBlank(parentView.getPropertyExpressions().get("headerText"))) {
                    header.getPropertyExpressions().put("headerText", parentView.getPropertyExpressions().get(
                            "headerText"));
                }

                KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(parentView,
                        header, currentModel, currentContext);
            }

            BreadcrumbItem breadcrumbItem = parentView.getBreadcrumbItem();

            if (breadcrumbItem != null) {
                KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(parentView,
                        breadcrumbItem, currentModel, currentContext);
            }

            if (pageBreadcrumbItem != null && pageBreadcrumbItem.getUrl() != null && StringUtils.isNotBlank(
                    pageBreadcrumbItem.getUrl().getPageId())) {
                PageGroup thePage = null;
                if (parentView.isSinglePageView() && parentView.getPage() != null) {
                    thePage = parentView.getPage();
                } else {
                    for (Component item : parentView.getItems()) {
                        if (item.getId().equals(pageBreadcrumbItem.getUrl().getPageId())) {
                            thePage = (PageGroup) item;
                            break;
                        }
                    }
                }

                if (thePage == null) {
                    //TODO throw error
                    return;
                }

                //populate from expression graph
                ExpressionUtils.populatePropertyExpressionsFromGraph(thePage, false);

                Header pageHeader = thePage.getHeader();

                if (pageHeader != null) {
                    if (StringUtils.isNotBlank(thePage.getPropertyExpressions().get("headerText"))) {
                        pageHeader.getPropertyExpressions().put("headerText", thePage.getPropertyExpressions().get(
                                "headerText"));
                    }

                    KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(parentView,
                            pageHeader, currentModel, currentContext);
                }

                BreadcrumbItem pageBreadcrumb = thePage.getBreadcrumbItem();

                if (pageBreadcrumb != null) {
                    KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(parentView,
                            pageBreadcrumb, currentModel, currentContext);
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("There was likely a problem evaluating an expression in a parent view or page"
                    + " because a property may not exist in the current context - explicitly set the label for this"
                    + " parentLocation: "
                    + parentView.getId(), e);
        }
    }

    /**
     * Evaluate any expressions that may have not been evaluated for the urls and breadcrumbItems of this
     * parentLocation
     * class using the currentModel and currentContext
     *
     * @param view the view
     * @param currentModel the current model
     * @param currentContext the current context
     */
    private void handleExpressions(View view, Object currentModel, Map<String, Object> currentContext) {
        try {
            //Evaluate view url/breadcrumb expressions
            KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(view,
                    viewBreadcrumbItem, currentModel, currentContext);

            if (viewBreadcrumbItem.getUrl() != null) {
                KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(view,
                        viewBreadcrumbItem.getUrl(), currentModel, currentContext);
            }

            if (parentViewUrl != null) {
                KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(view,
                        parentViewUrl, currentModel, currentContext);
            }

            //evaluate same for potential page properties
            if (pageBreadcrumbItem != null) {
                KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(view,
                        pageBreadcrumbItem, currentModel, currentContext);

                if (pageBreadcrumbItem.getUrl() != null) {
                    KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(view,
                            pageBreadcrumbItem.getUrl(), currentModel, currentContext);
                }
            }

            if (parentPageUrl != null) {
                KRADServiceLocatorWeb.getExpressionEvaluatorService().evaluateExpressionsOnConfigurable(view,
                        parentPageUrl, currentModel, currentContext);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("There was likely a problem evaluating an expression in a parent view or page"
                    + " because a property may not exist in the current context - problem in Url or BreadcrumbItem"
                    + " - set these to something that can be evaluated - of the parentLocation: "
                    + view.getId(), e);
        }
    }

    /**
     * Handle setting a page breadcrumbItem label when parentPageUrl is being used based on the PageGroup's
     * breadcrumbItem and header properties
     *
     * @param view the current view
     */
    private void handlePageBreadcrumb(View view, Object currentModel) {
        PageGroup thePage = null;
        if (view.isSinglePageView() && view.getPage() != null) {
            thePage = view.getPage();
        } else {
            for (Component item : view.getItems()) {
                if (item.getId().equals(pageBreadcrumbItem.getUrl().getPageId())) {
                    thePage = (PageGroup) item;
                    break;
                }
            }
        }

        if (thePage == null) {
            return;
        }

        //set label
        if (StringUtils.isBlank(pageBreadcrumbItem.getLabel()) && thePage.getBreadcrumbItem() != null &&
                StringUtils.isNotBlank(thePage.getBreadcrumbItem().getLabel())) {
            pageBreadcrumbItem.setLabel(thePage.getBreadcrumbItem().getLabel());
        } else if (StringUtils.isBlank(pageBreadcrumbItem.getLabel()) && StringUtils.isNotBlank(
                thePage.getHeaderText())) {
            pageBreadcrumbItem.setLabel(thePage.getHeaderText());
        }

        //page siblingBreadcrumb inheritance automation
        if (thePage.getBreadcrumbItem() != null
                && thePage.getBreadcrumbItem().getSiblingBreadcrumbComponent() != null
                && pageBreadcrumbItem.getSiblingBreadcrumbComponent() == null) {
            view.assignComponentIds(thePage.getBreadcrumbItem().getSiblingBreadcrumbComponent());

            pageBreadcrumbItem.setSiblingBreadcrumbComponent(
                    thePage.getBreadcrumbItem().getSiblingBreadcrumbComponent());
        }
    }

    /**
     * The parentViewUrl representing the url that is the parent of this View.
     *
     * <p>
     * This url can explicitly set an href
     * or can set a controller and viewId.  Parent view traversal is only performed if the controller and viewId
     * properties are set and NOT the explicit href (this affects if breadcrumbs are generated in a recursive chain).
     * </p>
     *
     * @return the parent view url
     */
    @BeanTagAttribute(name = "parentViewUrl", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public UrlInfo getParentViewUrl() {
        return parentViewUrl;
    }

    /**
     * Set the parentViewUrl
     *
     * @param parentViewUrl
     */
    public void setParentViewUrl(UrlInfo parentViewUrl) {
        this.parentViewUrl = parentViewUrl;
    }

    /**
     * The parentPageUrl representing a page url that is the parent of this View.  In order for automated label
     * determination to work for the page breadcrumbItem, the viewId and controllerMapping must match with the
     * parentViewUrl.
     *
     * <p>
     * This url can explicitly set an href or can set a pageId.  The parentViewUrl MUST be set before this option can
     * be set.  If the needed behavior is such that the parent view breadcrumbItem should not be shown and only this
     * item should be shown, set 'parentLocation.viewBreadcrumbItem.render' to false.
     * </p>
     *
     * @return the parent page url
     */
    @BeanTagAttribute(name = "parentPageUrl", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public UrlInfo getParentPageUrl() {
        return parentPageUrl;
    }

    /**
     * Set the parentPageUrl
     *
     * @param parentPageUrl
     */
    public void setParentPageUrl(UrlInfo parentPageUrl) {
        this.parentPageUrl = parentPageUrl;
    }

    /**
     * The parentViewLabel is the text used for breadcrumbItem label of the parent view.
     *
     * <p>
     * If not set, the the label is determined
     * by looking at the parent View's breadcrumbItem and then its headerText.  If the parent view's retrieved value
     * contain expressions, those expressions must be able to be evaluated in the current context (ie, the properties
     * they reference must also exist on the current form at the same location) or an exception will be thrown.
     * </p>
     *
     * @return the parentViewLabel set
     */
    @BeanTagAttribute(name = "parentViewLabel")
    public String getParentViewLabel() {
        return parentViewLabel;
    }

    /**
     * Set the parentViewLabel
     *
     * @param parentViewLabel
     */
    public void setParentViewLabel(String parentViewLabel) {
        this.parentViewLabel = parentViewLabel;
    }

    /**
     * The parentPageLabel is the text used for breadcrumbItem label of the parent page.
     *
     * <p>
     * If not set, the the label is determined
     * by looking at the parent PageGroup's breadcrumbItem and then its headerText.  This retrieval can only happen
     * if the parentViewUrl is set.
     * If the parent PageGroup's retrieved value
     * contain expressions, those expressions must be able to be evaluated in the current context (ie, the properties
     * they reference must also exist on the current form at the same location) or an exception will be thrown.
     * </p>
     *
     * @return the parentPageLabel set
     */
    @BeanTagAttribute(name = "parentPageLabel")
    public String getParentPageLabel() {
        return parentPageLabel;
    }

    /**
     * Set the parentPageLabel
     *
     * @param parentPageLabel
     */
    public void setParentPageLabel(String parentPageLabel) {
        this.parentPageLabel = parentPageLabel;
    }

    /**
     * The viewBreadcrumbItem to use for the parent location view breadcrumb.  Url should NOT be set here because
     * parentViewUrl is ALWAYS set into this breadcrumbItem, regardless of value.
     *
     * @return the viewBreadcrumbItem
     */
    @BeanTagAttribute(name = "viewBreadcrumbItem", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public BreadcrumbItem getViewBreadcrumbItem() {
        return viewBreadcrumbItem;
    }

    /**
     * Set the viewBreadcrumbItem
     *
     * @param breadcrumbItem
     */
    public void setViewBreadcrumbItem(BreadcrumbItem breadcrumbItem) {
        this.viewBreadcrumbItem = breadcrumbItem;
    }

    /**
     * The pageBreadcrumbItem to use for the parent location view breadcrumb.  Url should NOT be set here because
     * parentPageUrl is ALWAYS set into this breadcrumbItem, regardless of value.
     *
     * @return the pageBreadcrumbItem
     */
    @BeanTagAttribute(name = "pageBreadcrumbItem", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public BreadcrumbItem getPageBreadcrumbItem() {
        return pageBreadcrumbItem;
    }

    /**
     * Set the pageBreadcrumbItem
     *
     * @param pageBreadcrumbItem
     */
    public void setPageBreadcrumbItem(BreadcrumbItem pageBreadcrumbItem) {
        this.pageBreadcrumbItem = pageBreadcrumbItem;
    }

    /**
     * The resolved/generated breadcrumbItems determined by traversing the parentLocation chain.  These cannot be set
     * and must be generated by calling constructParentLocationBreadcrumbItems.
     *
     * @return the resolved breadcrumbItem list
     */
    public List<BreadcrumbItem> getResolvedBreadcrumbItems() {
        return resolvedBreadcrumbItems;
    }
}
