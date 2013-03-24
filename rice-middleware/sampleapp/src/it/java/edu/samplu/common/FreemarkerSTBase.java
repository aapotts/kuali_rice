/**
 * Copyright 2005-2013 The Kuali Foundation
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
package edu.samplu.common;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Freemarker loads properties from user defined properties file, if not available uses resource file.  Overrides properties
 * using a given key to identify them from JVM args. (i.e. -Dkey.name to override the name property in the key file.)
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class FreemarkerSTBase extends WebDriverLegacyITBase {

    protected final Logger LOG = Logger.getLogger(getClass());

    protected Configuration cfg;

    /**
     * Calls ftlWrite that also accepts a key, using the output getName as the key.
     * @param fileLocation
     * @param resourceLocation
     * @param output
     * @param template
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public File ftlWrite(String fileLocation, String resourceLocation, File output, Template template) throws IOException, TemplateException {
        return ftlWrite(fileLocation, resourceLocation, output.getName(), output, template);
    }

    /**
     * Loads properties from user defined properties file, if not available uses resource file
     *
     * writes processed template  to file
     * @param fileLocation
     * @param resourceLocation
     * @param key
     * @param output
     * @param template
     * @throws IOException
     * @throws TemplateException
     */
    public File ftlWrite(String fileLocation, String resourceLocation, String key, File output, Template template) throws IOException, TemplateException {
        Properties props = loadProperties(fileLocation, resourceLocation);
        systemPropertiesOverride(props, key);
        File outputFile = writeTemplateToFile(output, template, props);
        return outputFile;
    }

    protected Properties loadProperties(String fileLocation, String resourceLocation) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        if(fileLocation != null) {
            in = new FileInputStream(fileLocation);
        } else {
            in = getClass().getClassLoader().getResourceAsStream(resourceLocation);
        }
        if(in != null) {
            props.load(in);
            in.close();
        }
        return props;
    }

    /**
     *
     * @param file
     * @param template
     * @param props
     * @return
     * @throws IOException
     * @throws freemarker.template.TemplateException
     */
    protected File writeTemplateToFile(File file, Template template, Properties props) throws IOException, TemplateException {
        String output = FreeMarkerTemplateUtils.processTemplateIntoString(template, props);
        LOG.debug("Generated File Output: " + output);
        FileUtils.writeStringToFile(file, output);
        return file;
    }

    /**
     * -Dkey.propertyname= to override the property value for propertyname.
     * @param props
     */
    public void systemPropertiesOverride(Properties props, String key) {
        Enumeration<?> names = props.propertyNames();
        Object nameObject;
        String name;
        while (names.hasMoreElements()) {
            nameObject = names.nextElement();
            if (nameObject instanceof String) {
                name = (String)nameObject;
                LOG.debug("Overriding " + name + "=" + props.get(name) + " with " + props.getProperty(name) + " from JVM arg, " + key + "." + name);
                props.setProperty(name, System.getProperty(key + "." + name, props.getProperty(name)));
            }
        }
    }
}
