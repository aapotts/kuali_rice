/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.notification.kew;

import edu.iu.uis.eden.actionitem.ActionItem;
import edu.iu.uis.eden.actionlist.DisplayParameters;
import edu.iu.uis.eden.actions.ActionSet;
import edu.iu.uis.eden.plugin.attributes.CustomActionListAttribute;
import edu.iu.uis.eden.web.session.UserSession;

/**
 * This class is our custom action list for Notifications in KEW.  It's wired in as the implementation to use as part of the NotificationData.xml 
 * bootstrap file for KEW.
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class NotificationCustomActionListAttribute implements CustomActionListAttribute {
    /**
     * @see edu.iu.uis.eden.plugin.attributes.CustomActionListAttribute#getDocHandlerDisplayParameters(edu.iu.uis.eden.web.session.UserSession, edu.iu.uis.eden.actionitem.ActionItem)
     */
    public DisplayParameters getDocHandlerDisplayParameters(UserSession userSession, ActionItem actionItem) throws Exception {
	DisplayParameters dp = new DisplayParameters(new Integer(400));
	return dp;
    }

    /**
     * @see edu.iu.uis.eden.plugin.attributes.CustomActionListAttribute#getLegalActions(edu.iu.uis.eden.web.session.UserSession, edu.iu.uis.eden.actionitem.ActionItem)
     */
    public ActionSet getLegalActions(UserSession userSession,ActionItem actionItem) throws Exception {
	ActionSet as = new ActionSet();
	as.addFyi();
	return as;
    }

}
