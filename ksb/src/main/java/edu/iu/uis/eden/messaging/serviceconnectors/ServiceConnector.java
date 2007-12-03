/*
 * Copyright 2005-2006 The Kuali Foundation.
 * 
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
package edu.iu.uis.eden.messaging.serviceconnectors;

import org.kuali.rice.security.credentials.CredentialsSource;

/**
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 * @version $Revision: 1.3 $ $Date: 2007-12-03 02:51:29 $
 */
public interface ServiceConnector {
	
	Object getService() throws Exception;

	/**
	 * Method to set the optional CredentialsSource. A ServiceConnector is free
	 * to use the CredentialsSource to provide credentials to a remote service.

	 */
	void setCredentialsSource(CredentialsSource credentialsSource);

}
