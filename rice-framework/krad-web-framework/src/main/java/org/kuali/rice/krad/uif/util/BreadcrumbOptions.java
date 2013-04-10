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

import org.kuali.rice.krad.datadictionary.parse.BeanTag;
import org.kuali.rice.krad.datadictionary.parse.BeanTagAttribute;

import java.io.Serializable;
import java.util.List;

/**
 * BreadcrumbOptions represents the options for the current view breadcrumbs that are displayed.
 *
 * <p>
 * This class allows
 * for complete override of all breadcrumbs, and ability to add breadcrumbs before the view and page breadcrumb items.
 * Important note: breadcrumbOptions for preViewBreadcrumbs, prePageBreadcrumbs, and
 * breadcrumbOverrides are inherited from the View if not explicitly set from the PageGroup level's breadcrumbOptions
 * (if they contain a value at the view level and the property is null at the page level - default behavior).
 * Explicitly providing an empty list or setting these properties at the PageGroup level will
 * override this inheritance.
 * </p>
 */
@BeanTag(name = "breadcrumbOptions-bean", parent = "Uif-BreadcrumbOptions")
public class BreadcrumbOptions implements Serializable {

    private static final long serialVersionUID = -6705552809624394000L;

    //custom breadcrumbs
    private List<BreadcrumbItem> preViewBreadcrumbs;
    private List<BreadcrumbItem> prePageBreadcrumbs;
    private List<BreadcrumbItem> breadcrumbOverrides;

    /**
     * The preViewBreadcrumbs list represents BreadcrumbItems that will be shown before the View's BreadcrumbItem,
     * but after any parent location breadcrumbs/path based breadcrumbs (if in use)
     *
     * @return the preViewBreadcrumbs to render
     */
    @BeanTagAttribute(name = "preViewBreadcrumbs", type = BeanTagAttribute.AttributeType.LISTBEAN)
    public List<BreadcrumbItem> getPreViewBreadcrumbs() {
        return preViewBreadcrumbs;
    }

    /**
     * Set the preViewBreadcrumbs
     *
     * @param preViewBreadcrumbs
     */
    public void setPreViewBreadcrumbs(List<BreadcrumbItem> preViewBreadcrumbs) {
        this.preViewBreadcrumbs = preViewBreadcrumbs;
    }

    /**
     * The prePageBreadcrumbs list represents BreadcrumbItems that will be shown before the PageGroup's BreadcrumbItem,
     * but after the View's BreadcrumbItem.
     *
     * @return the preViewBreadcrumbs to render
     */
    @BeanTagAttribute(name = "prePageBreadcrumbs", type = BeanTagAttribute.AttributeType.LISTBEAN)
    public List<BreadcrumbItem> getPrePageBreadcrumbs() {
        return prePageBreadcrumbs;
    }

    /**
     * Set the prePageBreadcrumbs
     *
     * @param prePageBreadcrumbs
     */
    public void setPrePageBreadcrumbs(List<BreadcrumbItem> prePageBreadcrumbs) {
        this.prePageBreadcrumbs = prePageBreadcrumbs;
    }

    /**
     * The breadcrumbOverrides are a complete override for all breadcrumbs shown expect for parent location/path
     * breadcrumbs.
     *
     * <p>
     * The BreadcrumbItems set in this list will be used instead of any View, PageGroup, preViewBreadcrumbs, or
     * prePageBreadcrumbs BreadcrumbItems already set.  Each item can be customized fully.  If
     * parent location/path breadcrumbs should also not be shown, set renderParentLocations to false.
     * All other render options set in BreadcrumbOptions will be ignored/not apply as a result of setting this override
     * list.
     * </p>
     *
     * @return the breadcrumbOverride list
     */
    @BeanTagAttribute(name = "breadcrumbOverrides", type = BeanTagAttribute.AttributeType.LISTBEAN)
    public List<BreadcrumbItem> getBreadcrumbOverrides() {
        return breadcrumbOverrides;
    }

    /**
     * Set the breadcrumbOverrides list
     *
     * @param breadcrumbOverrides
     */
    public void setBreadcrumbOverrides(List<BreadcrumbItem> breadcrumbOverrides) {
        this.breadcrumbOverrides = breadcrumbOverrides;
    }
}
