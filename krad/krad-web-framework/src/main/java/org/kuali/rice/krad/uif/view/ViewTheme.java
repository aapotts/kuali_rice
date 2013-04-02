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
package org.kuali.rice.krad.uif.view;

import java.io.Serializable;
import java.util.List;

/**
 * Theme for the current view, currently just a list of stylesheets and js files, but has the potential
 * for expansion in the future
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ViewTheme implements Serializable{
    private static final long serialVersionUID = 7063256242857896580L;

    private List<String> stylesheets;
    private List<String> jsFiles;

    public List<String> getStylesheets() {
        return stylesheets;
    }

    public void setStylesheets(List<String> stylesheets) {
        this.stylesheets = stylesheets;
    }

    public List<String> getJsFiles() {
        return jsFiles;
    }

    public void setJsFiles(List<String> jsFiles) {
        this.jsFiles = jsFiles;
    }
}
