/**
 * Copyright 2005-2012 The Kuali Foundation
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
package org.kuali.rice.core;

import org.apache.log4j.Logger;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class);
    private long startTime;
    
    @Override
    protected void beforeRequest(HttpServletRequest httpServletRequest, String s) {
        if (loggableExtension(s)) {
            startTime = new Date().getTime();
        }
    }

    @Override
    protected void afterRequest(HttpServletRequest httpServletRequest, String s) {
        if (loggableExtension(s)) {
            long endTime = new Date().getTime();
            long elapsedTime = endTime - startTime;
            StringBuffer sb = new StringBuffer(s);
            sb.append(" ").append(elapsedTime).append(" ms.");
            LOG.info(sb.toString());
        }
    }

    private boolean loggableExtension(String s) {
        if (s.endsWith(".js]") || s.endsWith(".png]") || s.endsWith(".css]") || s.endsWith(".gif]")) {
            return false;
        }
        return true;
    }
}