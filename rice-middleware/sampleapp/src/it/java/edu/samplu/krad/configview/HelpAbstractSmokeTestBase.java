/*
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

package edu.samplu.krad.configview;

import edu.samplu.common.Failable;
import edu.samplu.common.ITUtil;
import edu.samplu.common.WebDriverLegacyITBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class HelpAbstractSmokeTestBase extends WebDriverLegacyITBase {

    /**
     * /kr-krad/configuration-test-view-uif-controller?viewId=ConfigurationTestView&methodToCall=start&pageId=ConfigurationTestView-Help-Page
     */
    public static final String BOOKMARK_URL = "/kr-krad/configuration-test-view-uif-controller?viewId=ConfigurationTestView&methodToCall=start&pageId=ConfigurationTestView-Help-Page";
   
    /**
     * Nav tests start at {@link edu.samplu.common.ITUtil#PORTAL}.  Bookmark Tests should override and return {@link HelpAbstractSmokeTestBase#BOOKMARK_URL}
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getTestUrl() {
        return ITUtil.PORTAL;
    }

    protected void navigation() throws Exception {
        waitAndClickKRAD();
        waitAndClickByXpath(CONFIGURATION_VIEW_XPATH);
        switchToWindow(CONFIGURATION_VIEW_WINDOW_TITLE);
        waitAndClickByLinkText("Help");
        Thread.sleep(5000);
        selectFrameIframePortlet();
    }

    protected void testHelpNav(Failable failable) throws Exception {
        // Call super tests else passed gets called before all tests are run.
        navigation();
        super.testViewHelp();
        navigation();
        super.testPageHelp();
        navigation();
        super.testTooltipHelp();
        navigation();
        super.testDisplayOnlyTooltipHelp();
        navigation();
        super.testMissingTooltipHelp();
    }

    protected void testHelpBookmark(Failable failable) throws Exception {
        // Call super tests else passed gets called before all tests are run.
        super.testViewHelp();
        super.testPageHelp();
        super.testTooltipHelp();
        super.testDisplayOnlyTooltipHelp();
        super.testMissingTooltipHelp();
        passed();
    }
    
    /**
     * Test the tooltip and external help on the view
     */
    public void testViewHelp() throws Exception {
        super.testViewHelp();
        passed();
    }

    /**
     * Test the tooltip and external help on the page
     */
    public void testPageHelp() throws Exception {
        super.testPageHelp();
        passed();
    }

    /**
     * Test the tooltip help on the section and fields
     */
    public void testTooltipHelp() throws Exception {
        super.testTooltipHelp();
        passed();
    }

    /**
     * Test the tooltip help on the sub-section and fields that are display only
     */
    public void testDisplayOnlyTooltipHelp() throws Exception {
        super.testDisplayOnlyTooltipHelp();
        passed();
    }

    /**
     * Test the tooltip help on the section and fields with no content
     */
    public void testMissingTooltipHelp() throws Exception {
        super.testMissingTooltipHelp();
        passed();
    }
}
