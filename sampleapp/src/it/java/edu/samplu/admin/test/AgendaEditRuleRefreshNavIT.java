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

import org.junit.Assert;
import org.junit.Test;

import edu.samplu.common.ITUtil;
import edu.samplu.common.WebDriverLegacyITBase;

/**
 * test that repeated ajax refreshes work
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class AgendaEditRuleRefreshNavIT extends WebDriverLegacyITBase {

    @Override
    public String getTestUrl() {
        return ITUtil.PORTAL;
    }

    /**
     * This overridden method ...
     * 
     * @see edu.samplu.common.WebDriverLegacyITBase#getTestUrl()
     */

    @Test
    /**
     * test that repeated ajax refreshes work
     */
    public void testAgendaEditRuleRefreshIT() throws Exception {
        waitAndClickByLinkText("Agenda Lookup");
        waitForPageToLoad();
        super.testAgendaEditRuleRefreshIT();
    }

}