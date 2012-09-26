package org.kuali.rice.krad.messages;

import org.junit.Test;
import org.kuali.rice.krad.messages.MessageService;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.test.data.PerSuiteUnitTestData;
import org.kuali.rice.test.data.UnitTestData;
import org.kuali.rice.test.data.UnitTestFile;
import org.kuali.test.KRADTestCase;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link org.kuali.rice.krad.messages.MessageService}
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@PerSuiteUnitTestData(
        value = @UnitTestData(
                order = {UnitTestData.Type.SQL_STATEMENTS, UnitTestData.Type.SQL_FILES},
                sqlFiles = {@UnitTestFile(filename = "classpath:testExternalMessages.sql", delimiter = ";")}))
public class MessageServiceTest extends KRADTestCase {

    private MessageService messageService;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        messageService = KRADServiceLocatorWeb.getMessageService();
    }

    /**
     * Test retrieval of a message from resources by key only (default namespace)
     */
    @Test
    public void testRetrieveMessage_ResourceByKey() throws Exception {
        String messageText = messageService.getMessageText("validation.test.error");

        assertEquals("Validation message text not correct", "App Error found for {0}", messageText);
    }

    /**
     * Test retrieval of a message from resources by namespace and key
     */
    @Test
    public void testRetrieveMessage_ResourceByNamespaceKey() throws Exception {
        String messageText = messageService.getMessageText("TEST", "", "validation.test.error");

        assertEquals("Validation message text not correct", "Error found for {0}", messageText);
    }

    /**
     * Test retrieval of a message from resources by namespace, component, and key
     */
    @Test
    public void testRetrieveMessage_ResourceByNamespaceComponentKey() throws Exception {
        String messageText = messageService.getMessageText("TEST", "TestComponent", "validation.test.error");

        assertEquals("Component validation message text not correct", "Component Error found", messageText);
    }

    /**
     * Test retrieval of a message from the database by key only (default namespace)
     */
    @Test
    public void testRetrieveMessage_DatabaseByKey() throws Exception {
        String messageText = messageService.getMessageText("validation.test2.error");

        assertEquals("Validation message text not correct", "App Error found for {0}", messageText);
    }

    /**
     * Test retrieval of a message from the database by namespace and key
     */
    @Test
    public void testRetrieveMessage_DatabaseByNamespaceKey() throws Exception {
        String messageText = messageService.getMessageText("TEST", "", "validation.test2.error");

        assertEquals("Validation message text not correct", "Error found for {0}", messageText);
    }

    /**
     * Test retrieval of a message from the database by namespace, component, and key
     */
    @Test
    public void testRetrieveMessage_DatabaseByNamespaceComponentKey() throws Exception {
        String messageText = messageService.getMessageText("TEST", "TestComponent", "validation.test2.error");

        assertEquals("Validation message text not correct", "Error found for {0}", messageText);
    }

    /**
     * Test retrieval of messages with different locales
     */
    @Test
    public void testRetrieveMessage_Locale() throws Exception {
        String messageText = messageService.getMessageText("TEST", "TestLocales", "message.key", "en-US");
        assertEquals("Message text not correct for English US locale", "English US Message", messageText);

        messageText = messageService.getMessageText("TEST", "TestLocales", "message.key", "fr-CA");
        assertEquals("Message text not correct for French Canada locale", "French CA Message", messageText);

        messageText = messageService.getMessageText("TEST", "TestLocales", "message.key", "de-AT");
        assertEquals("Message text not correct for German locale", "German Message", messageText);

        messageText = messageService.getMessageText("TEST", "TestLocales", "message.key", "de-DE");
        assertEquals("Message text not correct for German locale", "German Message", messageText);
    }
}
