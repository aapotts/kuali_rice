/*
 * Copyright 2007 The Kuali Foundation.
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
package edu.sampleu.travel.infrastructure;

import static org.kuali.rice.KNSServiceLocator.getDataDictionaryService;

import org.kuali.rice.KNSServiceLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TravelServiceLocator extends KNSServiceLocator {

    public static final String SPRING_BEANS = "classpath:edu/sampleu/travel/resources/SampleTravelAppModule.xml";

    private static TravelServiceLocator instance = new TravelServiceLocator();
    private ConfigurableApplicationContext applicationContext;

    private TravelServiceLocator() {
    }

    public static TravelServiceLocator getInstance() {
        return instance;
    }

    public void start() throws Exception {
        initializeApplicationContext();
    }

    public void stop() throws Exception {
        getInstance().getApplicationContext().close();
    }

    public static void initializeApplicationContext() throws Exception {
        getInstance().setApplicationContext(new ClassPathXmlApplicationContext(SPRING_BEANS));
        KNSServiceLocator.getInstance().setKnsApplicationContext(getInstance().getApplicationContext());
        getDataDictionaryService().completeInitialization();
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}