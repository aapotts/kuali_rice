/*
 * Copyright 2005-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
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
package edu.samplu.admin.test;

import java.io.File;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.Collections;
import java.util.List;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class IUeDocLiteXMLIngestNavIT extends AdminTmplMthdSTNavBase {
    // values set by default for repeatable testing; left as configurable for load tests
    private List<File> fileUploadList;

    private void setUpFiles() throws Exception {
        fileUploadList = new ArrayList<File>();
        // Load the directory as a resource
        // Turn the resource into a File object

        File dir = new File("src/it/resources/IU");

        if (dir != null && dir.listFiles().length > 0) {
            Integer i = 1;
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".xml")) {
                    if (!file.getName().equalsIgnoreCase("sample-app-config.xml"))
                        fileUploadList.add(file);
                }
                i++;
            }
            Collections.sort(fileUploadList);
        } else {
            throw new Exception("----Resources not found----");
        }
    }

    @Test
    public void test() throws Exception {
        try {
            setUpFiles();
        } catch (Exception e) {
            Assert.fail("Resources not found. Test will be skipped");
        }
        testXMLIngesterSuccessfulFileUpload();

        Thread.sleep(2000);
        driver.switchTo().defaultContent();
        waitAndClickByLinkText("Main Menu");
        waitAndClickByLinkText("eDoc Lite");

        driver.switchTo().frame("iframeportlet");
        waitAndClick(By.cssSelector("td.infoline > input[name=\"methodToCall.search\"]"));
        Thread.sleep(2000);
       
        driver.switchTo().defaultContent();

        waitAndClickByXpath("//input[@name='imageField' and @value='Logout']");
        passed();
    }

    /**
     * Uploads file available from fileUploadList through XML Ingester.
     * Uploads each sublist from main fileUploadList if size greater than 10. 
     * 
     */
    public void testXMLIngesterSuccessfulFileUpload() throws Exception {
        gotoMenuLinkLocator();
        if (fileUploadList != null && !fileUploadList.isEmpty()) {

            if (fileUploadList.size() > 10) {
                List<List<File>> subLists = getSubListsForFile(fileUploadList, 10);
                for (List<File> fileSet : subLists) {
                    fileIngester(fileSet);
                    for (File file : fileSet) {
                        assertTextPresent("Ingested xml doc: " + file.getName());
                    }
                }
            } else {
                fileIngester(fileUploadList);
            }
        }
    }

    /**
     * This overridden method ...
     * 
     * @see edu.samplu.common.NavTemplateMethodSTBase#getLinkLocator()
     */
    @Override
    protected String getLinkLocator() {

        return "XML Ingester";
    }

    /**
     * Performs Ingesting files to fileupload component and asserts succesful ingestion.
     * 
     */
    private void fileIngester(List<File> fileToUpload) throws Exception {
        int cnt = 0;
        for (File file : fileToUpload) {
            String path = file.getAbsolutePath().toString();
            driver.findElement(By.name("file[" + cnt + "]")).sendKeys(path);
            cnt++;
        }
        waitAndClickByXpath("//*[@id='imageField']");
        Thread.sleep(1000);
        // confirm all files were uploaded successfully
    }

    /**
     * Divides fileUploadList from resources into sublists to match the maximum number of file
     * upload components available on XML Ingester Screen
     * 
     */
    private List<List<File>> getSubListsForFile(List<File> fileList, final int L) {
        List<List<File>> subLists = new ArrayList<List<File>>();
        final int N = fileList.size();
        for (int i = 0; i < N; i += L) {
            subLists.add(new ArrayList<File>(fileList.subList(i, Math.min(N, i + L))));
        }
        return subLists;
    }
}