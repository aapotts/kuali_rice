/*
 * Copyright 2011 The Kuali Foundation
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
package edu.samplu.travel.krad.test;

import static org.junit.Assert.*;

import org.junit.Test;
import edu.samplu.common.WebDriverLegacyITBase;

/**
 * Test verifies updates in Totals at client side.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class CollectionTotallingLegacyIT extends WebDriverLegacyITBase {
    
    /**
     * This overridden method ...
     * 
     * @see edu.samplu.common.UpgradedSeleniumITBase#getTestUrl()
     */
    @Override
    public String getTestUrl() {
        //Returns "Group Totalling" url
        return "/kr-krad/uicomponents?viewId=Demo-CollectionTotaling&methodToCall=start";
    }

    @Test
    public void testCollectionTotalling() throws InterruptedException {
        
        //Scenario Asserts Changes in Total at client side        
        assertEquals("Total: 419", getText("div#Demo-CollectionTotaling-Section1 div[role='grid'] div[data-label='Total']"));
      
        clearText("div#Demo-CollectionTotaling-Section1 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section1]  input[name='list1[0].field1']");
        waitAndType("div#Demo-CollectionTotaling-Section1 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section1]  input[name='list1[0].field1']","10");
        waitAndClick("div#Demo-CollectionTotaling-Section1 div[role='grid'] div[data-label='Total']");

        Thread.sleep(5000);
        assertEquals("Total: 424", getText("div#Demo-CollectionTotaling-Section1 div[role='grid'] div[data-label='Total']"));
  
                
        //Scenario Asserts Changes in Total at client side on keyUp
        assertEquals("Total: 419", getText("div#Demo-CollectionTotaling-Section2 div[role='grid'] div[data-label='Total']"));
        clearText("div#Demo-CollectionTotaling-Section2 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section2] input[name='list1[0].field1']");        
        waitAndType("div#Demo-CollectionTotaling-Section2 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section2] input[name='list1[0].field1']","9");
        waitAndClick("div#Demo-CollectionTotaling-Section2 div[role='grid'] div[data-label='Total']");
        
        Thread.sleep(5000);
        assertEquals("Total: 423", getText("div#Demo-CollectionTotaling-Section2 div[role='grid'] div[data-label='Total']"));
        
        
        //Asserts absence of Total in 2nd column at the footer for Demonstrating totaling on only some columns 
        assertEquals("", getTextByXpath("//div[3]/div[3]/table/tfoot/tr/th[2]"));
        //Asserts Presence of Total in 2nd column at the footer for Demonstrating totaling on only some columns 
        assertEquals("Total: 369", getTextByXpath("//div[3]/div[3]/table/tfoot/tr/th[3]/div/fieldset/div/div[2]/div[2]"));
      
        
        //Asserts Presence of Total in left most column only being one with no totaling itself 
        assertEquals("Total:", getTextByXpath("//*[@id='u100213_span']"));
        assertEquals("419", getTextByXpath("//div[4]/div[3]/table/tfoot/tr/th[2]/div/fieldset/div/div[2]/div[2]"));

        //Asserts changes in value in Total and Decimal for Demonstrating multiple types of calculations for a single column (also setting average to 3 decimal places to demonstrate passing data to calculation function) 
        assertEquals("Total: 382", getTextByXpath("//div[2]/div/fieldset/div/div[2]/div[2]"));
        clearText("div#Demo-CollectionTotaling-Section6 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section6] input[name='list1[0].field4']");
        waitAndType("div#Demo-CollectionTotaling-Section6 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section6] input[name='list1[0].field4']","11");
        waitAndClick("div#Demo-CollectionTotaling-Section2 div[role='grid'] div[data-label='Total']");
        
        Thread.sleep(5000);
        assertEquals("Total: 385", getTextByXpath("//div[2]/div/fieldset/div/div[2]/div[2]"));
       
       // Assert changes in Decimal..
        clearText("div#Demo-CollectionTotaling-Section6 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section6] input[name='list1[0].field4']");
        waitAndType("div#Demo-CollectionTotaling-Section6 > div[role='grid'] > table > tbody div[data-parent=Demo-CollectionTotaling-Section6] input[name='list1[0].field4']","15.25");
        waitAndClick("div#Demo-CollectionTotaling-Section2 div[role='grid'] div[data-label='Total']");
      
        Thread.sleep(5000);
        assertEquals("Page Average: 11.917", getTextByXpath("//div[2]/fieldset/div/div[2]/div"));
       
    }  

}
