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
package edu.sampleu.travel.document.keyvalue;

import java.util.ArrayList;
import java.util.List;

import org.kuali.core.lookup.keyvalues.KeyValuesBase;
import org.kuali.core.web.ui.KeyLabelPair;

public class AccountTypeKeyValues extends KeyValuesBase {

    public List getKeyValues() {
        List keyValues = new ArrayList();

        keyValues.add(new KeyLabelPair("", ""));
        keyValues.add(new KeyLabelPair("CAT", "Clearing Account Type"));
        keyValues.add(new KeyLabelPair("EAT", "Expense Account Type"));
        keyValues.add(new KeyLabelPair("IAT", "Income Account Type"));

        return keyValues;
    }

}