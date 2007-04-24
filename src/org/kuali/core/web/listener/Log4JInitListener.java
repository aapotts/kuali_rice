/*
 * Copyright 2005-2006 The Kuali Foundation.
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
package org.kuali.core.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.kuali.core.Log4jConfigurer;
import org.kuali.core.util.log4j.StartupTimeStatsMailAppender;
import org.kuali.core.util.log4j.NDCFilter;

/**
 * This class is hte Log4J implementiion of the ServletContextListener.
 * 
 * 
 */

public class Log4JInitListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        Log4jConfigurer.configureLogging();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // this space intentionally left blank
    }
}