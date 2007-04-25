/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.core.service.impl;

import java.util.Date;

import org.kuali.core.service.ConfigurableDateService;

public class ConfigurableDateTimeServiceImpl extends DateTimeServiceImpl implements ConfigurableDateService {
    private Date currentDate;

    /**
     * Sets the currentDate attribute value.
     * 
     * @param currentDate The currentDate to set.
     */
    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    public Date getCurrentDate() {
        return currentDate;
    }
}
