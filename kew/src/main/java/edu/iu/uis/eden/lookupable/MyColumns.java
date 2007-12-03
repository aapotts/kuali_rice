/*
 * Copyright 2005-2006 The Kuali Foundation.
 * 
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iu.uis.eden.lookupable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A bean which wraps a List of {@link Column} objects.  Used in the
 * {@link RuleBaseValuesLookupableImpl}.
 * 
 * @see RuleBaseValuesLookupableImpl
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class MyColumns implements Serializable {

	private static final long serialVersionUID = -4669528607040709102L;
	private List columns;

    public MyColumns() {
        columns = new ArrayList();
    }

    public List getColumns() {
        return columns;
    }
    public void setColumns(List columns) {
        this.columns = columns;
    }
}
