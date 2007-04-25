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
package org.kuali.core.document;

import org.kuali.core.util.KualiDecimal;

/**
 * Any class that implements this interface should provide a document total.
 */
public interface AmountTotaling {
  
    /**
     * This method returns the total dollar amount for the document.
     * 
     * @return The total dollar amount as a KualiDecimal object instance.
     */
    public KualiDecimal getTotalDollarAmount();
}
