/*
 * Copyright 2005-2007 The Kuali Foundation.
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
package org.kuali.core.inquiry;

import java.util.List;
import java.util.Map;

import org.kuali.core.bo.BusinessObject;

/**
 * This interface defines the methods for inquirables.
 */
public interface Inquirable {
    public String getHtmlMenuBar();

    public String getTitle();

    public BusinessObject getBusinessObject(Map fieldValues);

    public List getSections(BusinessObject bo);

    public void setBusinessObjectClass(Class businessObjectClass);

    public void addAdditionalSections(List columns, BusinessObject bo);
}