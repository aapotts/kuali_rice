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
package edu.samplu.admin.test;

import edu.samplu.common.Failable;
import edu.samplu.common.ITUtil;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class IdentityPersonBlanketAppAbstractSmokeTestBase extends AdminTmplMthdSTNavBase{

    /**
     * ITUtil.PORTAL + "?channelTitle=Person&channelUrl=" 
     * + ITUtil.getBaseUrlString() + ITUtil.KNS_LOOKUP_METHOD + "org.kuali.rice.kim.api.identity.Person&docFormKey=88888888&returnLocation=" +
     * ITUtil.PORTAL_URL + ITUtil.HIDE_RETURN_LINK;
     */
    public static final String BOOKMARK_URL = ITUtil.PORTAL + "?channelTitle=Person&channelUrl=" 
            + ITUtil.getBaseUrlString() + ITUtil.KNS_LOOKUP_METHOD +
            "org.kuali.rice.kim.api.identity.Person&docFormKey=88888888&returnLocation=" +
            ITUtil.PORTAL_URL + ITUtil.HIDE_RETURN_LINK ;

    /**
     * {@inheritDoc}
     * Person
     * @return
     */
    @Override
    protected String getLinkLocator() {
        return "Person";
    }
   
    public void testIdentityPersonBlanketAppBookmark(Failable failable) throws Exception {
        testIdentityPersonBlanketApprove();
        passed();
    }

    public void testIdentityPersonBlanketAppNav(Failable failable) throws Exception {
        gotoMenuLinkLocator();
        testIdentityPersonBlanketApprove();
        passed();
    }
}
