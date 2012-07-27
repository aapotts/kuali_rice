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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.samplu.common.UpgradedSeleniumITBase;
import org.junit.Test;

/**
 * tests that user 'admin', on blanket approving a new Responsibility maintenance document, results in a final document
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class IdentityResponsibilityBlanketAppIT extends UpgradedSeleniumITBase {

    public static final String LABEL_KUALI_KUALI_SYSTEMS = "label=KUALI - Kuali Systems";

    @Override
    public String getTestUrl() {
        return PORTAL;
    }

    @Test
    public void testResponsibility() throws Exception {
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.click("link=Administration");
        selenium.waitForPageToLoad("30000");
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.click("link=Responsibility");
        selenium.waitForPageToLoad("30000");
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.selectFrame("iframeportlet");
        selenium.click("//img[@alt='create new']");
        selenium.waitForPageToLoad("30000");
        String docId = selenium.getText("//div[@id='headerarea']/div/table/tbody/tr[1]/td[1]");
        selenium.type("//input[@id='document.documentHeader.documentDescription']", "Validation Test Responsibility");
        selenium.select("//select[@id='document.newMaintainableObject.namespaceCode']", LABEL_KUALI_KUALI_SYSTEMS);
        selenium.type("//input[@id='document.newMaintainableObject.name']", "Validation Test Responsibility1");
        selenium.type("//input[@id='document.newMaintainableObject.documentTypeName']", "Test");
        selenium.type("//input[@id='document.newMaintainableObject.routeNodeName']", "Test");
        selenium.click("//input[@id='document.newMaintainableObject.actionDetailsAtRoleMemberLevel']");
        selenium.click("//input[@id='document.newMaintainableObject.required']");
        selenium.click("methodToCall.blanketApprove");
        selenium.waitForPageToLoad("30000");
        selenium.selectWindow("null");
        selenium.click("//img[@alt='doc search']");
        selenium.waitForPageToLoad("30000");
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.selectFrame("iframeportlet");
        selenium.click("//input[@name='methodToCall.search' and @value='search']");
        selenium.waitForPageToLoad("30000");
        docId= "link=" + docId;
        assertTrue(selenium.isElementPresent(docId));
        // KULRICE-7748 : IdentityResponsibilityBlanketAppIT fails expected:<[ENROUTE]> but was:<[FINAL]>
        if(selenium.isElementPresent(docId)){            
            assertEquals("ENROUTE", selenium.getText("//table[@id='row']/tbody/tr[1]/td[4]"));
        }else{
            assertEquals(docId, selenium.getText("//table[@id='row']/tbody/tr[1]/td[1]"));
            assertEquals("ENROUTE", selenium.getText("//table[@id='row']/tbody/tr[1]/td[4]"));
        }
    }
}
