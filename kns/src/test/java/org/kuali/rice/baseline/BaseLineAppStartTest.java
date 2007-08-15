/*
 * Copyright 2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.baseline;

import java.net.URL;

import org.junit.Test;
import org.kuali.rice.testharness.HtmlUnitUtil;
import org.kuali.rice.testharness.KNSTestCase;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class BaseLineAppStartTest extends KNSTestCase {

    @Test public void testHomePage() throws Exception {
        final WebClient webClient = new WebClient();
        final URL url = new URL(HtmlUnitUtil.BASE_URL);
        final HtmlPage page = (HtmlPage)webClient.getPage(url);
        assertEquals("Rice Sample Client", page.getTitleText() );
    }
    
}
