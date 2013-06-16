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
public abstract class Help2AbstractSmokeTestBase extends WebDriverLegacyITBase {

    /**
     * /kr-krad/configuration-test-view-uif-controller?viewId=ConfigurationTestView-Help&methodToCall=start
     */
    public static final String BOOKMARK_URL = "/kr-krad/configuration-test-view-uif-controller?viewId=ConfigurationTestView-Help&methodToCall=start";
   
    /**
     * Nav tests start at {@link edu.samplu.common.ITUtil#PORTAL}.  Bookmark Tests should override and return {@link Help2AbstractSmokeTestBase#BOOKMARK_URL}
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

    protected void testHelp2Nav(Failable failable) throws Exception {
        navigation();
        testViewHelp2();
        navigation();
        testExternalHelp2();
        navigation();
        testDisplayOnlyExternalHelp2();
        navigation();
        testMissingExternalHelp2();
        passed();
    }

    protected void testHelp2Bookmark(Failable failable) throws Exception {
        testViewHelp();
        testPageHelp();
        testTooltipHelp();
        testDisplayOnlyTooltipHelp();
        testMissingTooltipHelp();
        passed();
    }
}
