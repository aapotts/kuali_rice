/**
 * Copyright 2005-2012 The Kuali Foundation
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
package edu.sampleu.demo.kitchensink;

import org.kuali.rice.krad.web.form.UifFormBase;

import javax.servlet.http.HttpServletRequest;

/**
 * Form for Test UI Page
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class UifDialogTestForm extends UifFormBase {
    private static final long serialVersionUID = -7525378097732916418L;

    private String field1;
    private String field2;
    private String field3;

    private boolean bField1;

    public UifDialogTestForm() {
        super();
    }

    @Override
    public void postBind(HttpServletRequest request) {
        super.postBind(request);
    }

    /**
     * @return the field1
     */
    public String getField1() {
        return this.field1;
    }

    /**
     * @param field1 the field1 to set
     */
    public void setField1(String field1) {
        this.field1 = field1;
    }


    /**
     * @return the field2
     */
    public String getField2() {
        return this.field2;
    }

    /**
     * @param field2 the field2 to set
     */
    public void setField2(String field2) {
        this.field2 = field2;
    }

    /**
     * @return the field3
     */
    public String getField3() {
        return this.field3;
    }

    /**
     * @param field3 the field3 to set
     */
    public void setField3(String field3) {
        this.field3 = field3;
    }

    /**
     * @return the bField1
     */
    public boolean isbField1() {
        return this.bField1;
    }

    /**
     * @param bField1 the bField1 to set
     */
    public void setbField1(boolean bField1) {
        this.bField1 = bField1;
    }

}