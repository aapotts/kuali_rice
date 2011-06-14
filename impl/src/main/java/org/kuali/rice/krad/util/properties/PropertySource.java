/*
 * Copyright 2005-2007 The Kuali Foundation
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
package org.kuali.rice.krad.util.properties;

import java.util.Properties;

import org.kuali.rice.krad.exception.PropertiesException;

/**
 * This is an interface for a source for properties
 * 
 * 
 */
public interface PropertySource {
    /**
     * @return Properties loaded from this PropertySource
     * @throws PropertiesException if there's a problem loading the properties
     */
    public Properties loadProperties();
}
