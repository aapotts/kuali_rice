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
package edu.samplu.common;

import edu.samplu.admin.test.ComponentAbstractSmokeTestBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created as a way to link Rice Smoke Test failures to existing Jiras as a html link in Jenkins.  The more failures
 * the more useful it is to not have to keep tracking down the same Jiras.  Having this feature for Integration Tests
 * as well would be a huge help for the QA team.
 * TODO:
 * <ol>
 *   <li>Integration Test integration.  ITs often fail by the 10s tracking down existing Jiras is a huge time sink.</li>
 *   <li>Possible Extraction of jiraMatches data to property file.</li>
 * </ol>
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class JiraAwareFailureUtil {
    /**
     * KULRICE-8823 Fix broken smoke tests in CI
     */
    public static final String KULRICE_8823_FIX_BROKEN_SMOKE_TESTS_IN_CI = "KULRICE-8823 Fix broken smoke tests in CI";

    /**
     * https://jira.kuali.org/browse/
     */
    public static final String JIRA_BROWSE_URL = "https://jira.kuali.org/browse/";

    static Map<String, String> jiraMatches;

    static {
        jiraMatches = new HashMap<String, String>();

        jiraMatches.put(ComponentAbstractSmokeTestBase.CREATE_NEW_DOCUMENT_NOT_SUBMITTED_SUCCESSFULLY_MESSAGE_TEXT + ComponentAbstractSmokeTestBase.FOR_TEST_MESSAGE,
                KULRICE_8823_FIX_BROKEN_SMOKE_TESTS_IN_CI);

        jiraMatches.put("//*[@id='u229']", KULRICE_8823_FIX_BROKEN_SMOKE_TESTS_IN_CI);

        jiraMatches.put("//a[contains(text(),'Travel Account Lookup')])[3]", KULRICE_8823_FIX_BROKEN_SMOKE_TESTS_IN_CI);

        jiraMatches.put("By.linkText: Travel Account Lookup", KULRICE_8823_FIX_BROKEN_SMOKE_TESTS_IN_CI);

        jiraMatches.put("//a[contains(text(),'Validation - Regex')", KULRICE_8823_FIX_BROKEN_SMOKE_TESTS_IN_CI);

        //        jiraMatches.put("",
        //                "");
    }

    /**
     * If the contents contents the jiraMatches key, call fail on failable passing in the jiraMatches value for the matched key.
     * @param contents to check for containing of the jiraMatches keys.
     * @param failable to fail with the jiraMatches value if the jiraMatches key is contained in the contents
     */
    public static void failOnMatchedJira(String contents, Failable failable) {
        Iterator<String> iter = jiraMatches.keySet().iterator();
        String key = null;

        while (iter.hasNext()) {
            key = iter.next();
            if (contents.contains(key)) {
                failable.fail(JIRA_BROWSE_URL + jiraMatches.get(key));
            }
        }
    }

    /**
     * Calls failOnMatchedJira with the contents and if no match is detected then the message.
     * @param contents to check for containing of the jiraMatches keys.
     * @param message to check for containing of the jiraMatches keys if contents doesn't
     * @param failable to fail with the jiraMatches value if the contents or message is detected
     */
    public static void failOnMatchedJira(String contents, String message, Failable failable) {
        failOnMatchedJira(contents, failable);
        failOnMatchedJira(message, failable);
    }
}
