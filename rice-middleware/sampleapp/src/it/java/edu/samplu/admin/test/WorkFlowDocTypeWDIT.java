/**
 * Copyright 2005-2011 The Kuali Foundation
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
package edu.samplu.admin.test;

import org.junit.Test;

import edu.samplu.common.ITUtil;
import edu.samplu.common.WebDriverLegacyITBase;

/**
 * tests creating and cancelling new and edit Document Type maintenance screens 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class WorkFlowDocTypeWDIT extends WebDriverLegacyITBase{
    public static final String TEST_URL = ITUtil.PORTAL + "?channelTitle=Document%20Type&channelUrl=" + ITUtil.getBaseUrlString() +
            "/kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kew.doctype.bo.DocumentType&returnLocation=" +
            ITUtil.PORTAL_URL + "&hideReturnLink=true&docFormKey=88888888";
  
    @Override
    public String getTestUrl() {
        return TEST_URL;
    }
    @Test
    /**
     * tests that a new Document Type maintenance document can be cancelled
     */
    public void testCreateNew() throws Exception {
        super.testCreateNewCancel();
    }
    
    @Test
    /**
     * tests that a Document Type maintenance document is created for an edit operation originating from a lookup screen
     */
    public void testEditDocType() throws Exception {
       super.testSearchEditCancel();
    }
    
    //Test to validate the requirement of Document Type Label field while submitting a document.
    @Test
    public void testCreateDocType() throws Exception {
        super.testCreateDocType();
    }
}
