package edu.samplu.admin.test;

import edu.samplu.common.AdminMenuBlanketAppLegacyITBase;
import edu.samplu.common.ITUtil;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */

public class ConfigNameSpaceBlanketAppLegacyIT extends AdminMenuBlanketAppLegacyITBase {

    @Override
    protected String getLinkLocator() {
        return "Namespace";
    }

    @Override
    public String blanketApprove() throws Exception {
        String docId = waitForDocId();
        waitAndTypeByXpath("//input[@id='document.documentHeader.documentDescription']", "Validation Test Namespace");
        assertBlanketApproveButtonsPresent();
        waitAndTypeByXpath("//input[@id='document.newMaintainableObject.code']", "VTN" + ITUtil.DTS);
        waitAndTypeByXpath("//input[@id='document.newMaintainableObject.name']",
                "Validation Test NameSpace " + ITUtil.DTS);
        waitAndTypeByXpath("//input[@id='document.newMaintainableObject.applicationId']", "RICE");
        return docId;
    }

    private void assertBlanketApproveButtonsPresent() {
        assertElementPresentByName("methodToCall.route");
        assertElementPresentByName("methodToCall.save");
        assertElementPresentByName("methodToCall.blanketApprove");
        assertElementPresentByName("methodToCall.close");
        assertElementPresentByName("methodToCall.cancel");
    }

}