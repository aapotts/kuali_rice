/*
 * Copyright 2006-2012 The Kuali Foundation
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

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleneseTestCase;
import junit.framework.Assert;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.regex.Pattern;

public class ServerWarningsIT{
    private Selenium selenium;


    public static String getBaseUrlString() {
        String baseUrl = System.getProperty("remote.public.url");
        if (baseUrl == null) {
            baseUrl = "http://localhost:8080";
        } else if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else if (!baseUrl.startsWith("http")) {
            baseUrl = "http://" + baseUrl;
        }
        return baseUrl;
    }

    public static void login(Selenium selenium) {
        Assert.assertEquals("Login", selenium.getTitle());
        selenium.type("__login_user", "admin");
        selenium.click("//input[@value='Login']");
        selenium.waitForPageToLoad("30000");
    }


    @Before
    public void setUp() throws Exception {
        String baseUrl = getBaseUrlString();
        WebDriver driver = new FirefoxDriver();
        selenium = new WebDriverBackedSelenium(driver,
                baseUrl + "/kr-krad/uicomponents?viewId=Demo-ValidationLayout&methodToCall=start");

        // Login
        selenium.open(
                baseUrl + "/kr-krad/uicomponents?viewId=Demo-ValidationLayout&methodToCall=start");
        login(selenium);
    }

	@Test
	public void testServerWarningsIT() throws Exception {
		selenium.click("//button[contains(.,'Get Warning Messages')]");
		selenium.waitForPageToLoad("30000");
		Assert.assertTrue(selenium.isVisible("css=div[data-messagesfor=\"Demo-ValidationLayout-SectionsPage\"]"));
		Assert.assertTrue(selenium.isElementPresent("css=div[data-messagesfor=\"Demo-ValidationLayout-SectionsPage\"] .uif-warningMessageItem"));
		Assert.assertTrue(selenium.isVisible("css=div[data-messagesfor=\"Demo-ValidationLayout-Section1\"]"));
		Assert.assertTrue(selenium.isElementPresent("css=div[data-messagesfor=\"Demo-ValidationLayout-Section1\"] .uif-warningMessageItem"));
		Assert.assertTrue(selenium.isElementPresent("css=div[data-role=\"InputField\"] img[alt=\"Warning\"]"));
		selenium.mouseOver("//a[contains(.,'Field 1')]");
		Assert.assertTrue(selenium.isElementPresent("css=.uif-warningHighlight"));
		selenium.click("//a[contains(.,'Field 1')]");
		for (int second = 0;; second++) {
			if (second >= 60) Assert.fail("timeout");
			try { if (selenium.isVisible("css=.jquerybubblepopup-innerHtml")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		Assert.assertTrue(selenium.isVisible("css=.jquerybubblepopup-innerHtml > .uif-serverMessageItems"));
		Assert.assertTrue(selenium.isVisible("css=.jquerybubblepopup-innerHtml > .uif-serverMessageItems .uif-warningMessageItem-field"));
		selenium.type("name=field1", "");
		selenium.fireEvent("name=field1", "blur");
		selenium.fireEvent("name=field1", "focus");
		for (int second = 0;; second++) {
			if (second >= 60) Assert.fail("timeout");
			try { if (selenium.isVisible("css=.jquerybubblepopup-innerHtml")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		Assert.assertTrue(selenium.isVisible("css=.jquerybubblepopup-innerHtml > .uif-serverMessageItems .uif-warningMessageItem-field"));
		for (int second = 0;; second++) {
			if (second >= 60) Assert.fail("timeout");
			try { if (selenium.isVisible("css=.jquerybubblepopup-innerHtml > .uif-clientMessageItems")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		Assert.assertTrue(selenium.isVisible("css=.jquerybubblepopup-innerHtml > .uif-clientMessageItems  .uif-errorMessageItem-field"));
		selenium.type("name=field1", "b");
		selenium.keyDown("name=field1", "b");
		selenium.keyUp("name=field1", "b");
        selenium.typeKeys("name=field1", "\b\b\b");
		for (int second = 0;; second++) {
			if (second >= 60) Assert.fail("timeout");
			try { if (!selenium.isElementPresent("css=.jquerybubblepopup-innerHtml > .uif-clientMessageItems")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		Assert.assertTrue(selenium.isVisible("css=.jquerybubblepopup-innerHtml > .uif-serverMessageItems .uif-warningMessageItem-field"));
		Assert.assertFalse(selenium.isElementPresent("css=.jquerybubblepopup-innerHtml > .uif-clientMessageItems"));
		selenium.type("name=field1", "");
		selenium.fireEvent("name=field1", "focus");
		selenium.fireEvent("name=field1", "blur");
		Assert.assertTrue(selenium.isElementPresent("css=.uif-hasError"));
		Assert.assertTrue(selenium.isElementPresent("css=img[src*=\"error.png\"]"));
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
