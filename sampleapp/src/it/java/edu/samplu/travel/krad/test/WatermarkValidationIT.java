/*
 * Copyright 2006-2011 The Kuali Foundation
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
package edu.samplu.travel.krad.test;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import edu.samplu.common.ITUtil;
import edu.samplu.common.WebDriverITBase;

/**
 * tests whether the watermarks is work as expected even when they contain an
 * apostrophe
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class WatermarkValidationIT extends WebDriverITBase {
	@Override
	public String getTestUrl() {
		return ITUtil.PORTAL;
	}

	@Test
	/**
	 * if watermarking is ok, the cancel link will bring up a confirmation if something was typed into a textbox i.e
	 * the scripts will be working ok
	 */
	public void testWatermarking() throws Exception {
		waitAndClickByLinkText("KRAD");
		Thread.sleep(5000);
		waitAndClickByLinkText("Uif Components (Kitchen Sink)");
		Thread.sleep(5000);
		Thread.sleep(2000);
		//Switch to new window.
		switchWindow();
		Thread.sleep(3000);
		assertEquals("It's watermarked ",getAttributeValueByName("field106", "placeholder"));
		assertEquals("Watermark... ",getAttributeValueByName("field110", "placeholder"));
	}
}
