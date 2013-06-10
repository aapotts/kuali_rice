/**
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
package edu.samplu.krad.validationmessagesview;

import edu.samplu.common.Failable;
import edu.samplu.common.ITUtil;
import edu.samplu.common.WebDriverLegacyITBase;

/**
 * Tests the Component section in Rice.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class ServerInfoAbstractSmokeTestBase extends WebDriverLegacyITBase {

    /**
     * "/kr-krad/uicomponents?viewId=Demo-ValidationLayout&methodToCall=start"
     */
    public static final String BOOKMARK_URL = "/kr-krad/uicomponents?viewId=Demo-ValidationLayout&methodToCall=start";

    protected void bookmark() {
        open(ITUtil.getBaseUrlString() + BOOKMARK_URL);
    }

    /**
     * Nav tests start at {@link edu.samplu.common.ITUtil#PORTAL}.
     * Bookmark Tests should override and return {@link edu.samplu.krad.validationmessagesview.ServerInfoAbstractSmokeTestBase#BOOKMARK_URL}
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getTestUrl() {
        return ITUtil.PORTAL;
    }

    protected void navigation() throws InterruptedException {
        waitAndClickKRAD();
        waitAndClickByXpath(VALIDATION_FRAMEWORK_DEMO_XPATH);
        switchToWindow(KUALI_VIEW_WINDOW_TITLE);
    }

    protected void testServerInfoNav(Failable failable) throws Exception {
        navigation();
        testServerInfoIT();
        passed();
    }

    protected void testServerInfoBookmark(Failable failable) throws Exception {
        testServerInfoIT();
        passed();
    }    
}
