package edu.samplu.common;

import com.thoughtworks.selenium.Selenium;
import org.junit.Assert;

import java.util.Calendar;

import static com.thoughtworks.selenium.SeleneseTestBase.fail;

/**
 * Common selenium test methods that should be reused rather than recreated for each test.
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */

public class ITUtil {

    public static String DTS = Calendar.getInstance().getTime().getTime() + "";
    public static String WAIT_TO_END_TEST = "5000";

    /**
     * In order to run as a smoke test the ability to set the baseUrl via the JVM arg remote.public.url is required.
     * Trailing slashes are trimmed.  If the remote.public.url does not start with http:// it will be added.
     * @return http://localhost:8080 by default else the value of remote.public.url
     */
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

    /**
     * If the JVM arg remote.autologin is set, auto login as admin will not be done.
     * @param selenium to login with
     */
    public static void login(Selenium selenium) {
        if (System.getProperty("remote.autologin") == null) {
            if (!"Login".equals(selenium.getTitle())) {
                fail("Title is not Login as expected, but " + selenium.getTitle());
            }
            selenium.type("__login_user", "admin");
            selenium.click("//input[@value='Login']");
            selenium.waitForPageToLoad("30000");
        }
    }

    /**
     * Setting the JVM arg remote.driver.dontTearDown to y or t leaves the browser window open when the test has completed.  Valuable when debugging, updating, or creating new tests.
     * When implementing your own tearDown method rather than an inherited one, it is a common courtesy to include this check and not stop and shutdown the browser window to make it easy debug or update your test.
     * {@code }
     * @return true if the dontTearDownProperty is not set.
     */
    public static boolean dontTearDownPropertyNotSet() {
        return System.getProperty("remote.driver.dontTearDown") == null ||
                "f".startsWith(System.getProperty("remote.driver.dontTearDown").toLowerCase()) ||
                "n".startsWith(System.getProperty("remote.driver.dontTearDown").toLowerCase());
    }

    /**
     * Wait 60 seconds for the elementLocator to be present or fail.  Click if present
     * @param selenium
     * @param elementLocator
     * @throws InterruptedException
     */
    public static void waitAndClick(Selenium selenium, String elementLocator) throws InterruptedException {
        waitAndClick(selenium, elementLocator, 60);
    }

    /**
     * Wait the given seconds for the elementLocator to be present or fail
     * @param selenium
     * @param elementLocator
     * @param seconds
     * @throws InterruptedException
     */
    public static void waitAndClick(Selenium selenium, String elementLocator, int seconds) throws InterruptedException {
        waitForElement(selenium, elementLocator, 60);
        selenium.click(elementLocator);
        Thread.sleep(1000);
    }


    /**
     * Wait 60 seconds for the elementLocator to be present or fail
     * @param selenium
     * @param elementLocator
     * @throws InterruptedException
     */
    public static void waitForElement(Selenium selenium, String elementLocator) throws InterruptedException {
        waitForElement(selenium, elementLocator, 60);
    }

    /**
     * Wait the given seconds for the elementLocator to be present or fail
     * @param selenium
     * @param elementLocator
     * @param seconds
     * @throws InterruptedException
     */
    public static void waitForElement(Selenium selenium, String elementLocator, int seconds) throws InterruptedException {
        for (int second = 0;; second++) {
            if (second >= seconds) fail("timeout of " + seconds + " seconds waiting for " + elementLocator);
            try { if (selenium.isElementPresent(elementLocator)) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }
    }

    /**
     * Fails if a Incident Report is detected, extracting and reporting the View Id, Document Id, and StackTrace
     * @param selenium
     * @param linkLocator used only in the faillure message
     */
    public static void checkForIncidentReport(Selenium selenium, String linkLocator) {
        String contents = selenium.getHtmlSource();
        if (contents.contains("Incident Report") && !contents.contains("SeleniumException")) { // selenium timeouts have Incident Report in them
            String chunk =  contents.substring(contents.indexOf("Incident Feedback"), contents.lastIndexOf("</div>") );
            String docId = chunk.substring(chunk.lastIndexOf("Document Id"), chunk.indexOf("View Id"));
            docId = docId.substring(0, docId.indexOf("</span>"));
            docId = docId.substring(docId.lastIndexOf(">") + 2, docId.length());

            String viewId = chunk.substring(chunk.lastIndexOf("View Id"), chunk.indexOf("Error Message"));
            viewId = viewId.substring(0, viewId.indexOf("</span>"));
            viewId = viewId.substring(viewId.lastIndexOf(">") + 2, viewId.length());

            String stackTrace = chunk.substring(chunk.lastIndexOf("(only in dev mode)"), chunk.length());
            stackTrace = stackTrace.substring(stackTrace.indexOf("<span id=\"") + 3, stackTrace.length());
            stackTrace = stackTrace.substring(stackTrace.indexOf("\">") + 2, stackTrace.indexOf("</span>"));

            //            System.out.println(docId);
            //            System.out.println(viewId);
            //            System.out.println(stackTrace);
            Assert.fail("Incident report navigating to "
                    + linkLocator
                    + " : View Id: "
                    + viewId.trim()
                    + " Doc Id: "
                    + docId.trim()
                    + "\nStackTrace: "
                    + stackTrace.trim());
        }
    }
}