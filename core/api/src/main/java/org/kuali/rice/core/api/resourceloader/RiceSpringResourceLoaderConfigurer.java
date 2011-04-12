/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.core.api.resourceloader;

import javax.xml.namespace.QName;

import org.kuali.rice.core.api.config.ConfigurationException;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Wraps {@link BeanFactory} in {@link BeanFactoryResourceLoader} and places the {@link ResourceLoader} 
 * at the top of the {@link GlobalResourceLoader} stack.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class RiceSpringResourceLoaderConfigurer implements BeanFactoryAware, InitializingBean {
	
	private QName name;
	private String localServiceName;
	private String serviceNameSpaceURI;

	private BeanFactory beanFactory;

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.name == null) {
			if (this.getServiceNameSpaceURI() == null) {
				this.setServiceNameSpaceURI(ConfigContext.getCurrentContextConfig().getServiceNamespace());
			}
			if (this.getLocalServiceName() == null) {
				throw new ConfigurationException("Need to give " + this.getClass().getName() + " a LocalServiceName");
			}
		}
		
		ResourceLoader beanFactoryRL = new BeanFactoryResourceLoader(getName(), this.beanFactory);
		GlobalResourceLoader.addResourceLoaderFirst(beanFactoryRL);
	}

	public QName getName() {
		if (this.name == null) {
			this.setName(new QName(this.getServiceNameSpaceURI(), this.getLocalServiceName()));
		}
		return name;
	}

	public void setName(QName name) {
		this.name = name;
	}
	
	public String getLocalServiceName() {
		return localServiceName;
	}

	public void setLocalServiceName(String localServiceName) {
		this.localServiceName = localServiceName;
	}

	public String getServiceNameSpaceURI() {
		return serviceNameSpaceURI;
	}

	public void setServiceNameSpaceURI(String serviceNameSpaceURI) {
		this.serviceNameSpaceURI = serviceNameSpaceURI;
	}

}
