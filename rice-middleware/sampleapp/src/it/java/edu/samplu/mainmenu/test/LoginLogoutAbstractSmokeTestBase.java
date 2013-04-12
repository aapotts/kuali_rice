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
package edu.samplu.mainmenu.test;

import edu.samplu.common.Failable;
import edu.samplu.common.ITUtil;
import edu.samplu.common.WebDriverLegacyITBase;

/**
 * Abstract base class for LoginLogout Smoke Tests.  Framework specific classes should not be depended upon in this
 * class but abstracted behind {@link edu.samplu.common.Failable}.
 *
 * @see edu.samplu.common.Failable
 * @see LoginLogoutSTNGBase
 * @see LoginLogoutSTJUnitBase
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class LoginLogoutAbstractSmokeTestBase extends WebDriverLegacyITBase {

    /**
     * LoginLogout can use {@link ITUtil#PORTAL} for both navigation and bookmark tests so it will not be overridden by subclasses.
     * @return {@link ITUtil#PORTAL}
     */
    @Override
    public String getTestUrl() {
        return ITUtil.PORTAL;
    }

    private void navigate(Failable failable) throws InterruptedException {
        waitAndClickMainMenu(failable);
        waitForPageToLoad();
    }

    /**
     * Navigate to the page under test click logout.  {@link LoginLogoutAbstractSmokeTestBase#waitAndClickLogout(edu.samplu.common.Failable)}
     *
     * @param failable {@link edu.samplu.common.Failable}
     * @throws Exception
     */
    public void testLogoutNav(Failable failable) throws Exception {
        navigate(failable);
        waitAndClickLogout(failable);
        passed();
    }

    /**
     * Click Logout on the current page.  {@link LoginLogoutAbstractSmokeTestBase#waitAndClickLogout(edu.samplu.common.Failable)}
     *
     * @param failable {@link edu.samplu.common.Failable}
     * @throws Exception
     */
    public void testLogoutBookmark(Failable failable) throws Exception {
        waitAndClickLogout(failable);
        passed();
    }
}
