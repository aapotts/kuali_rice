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
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * FreemarkerUtil loads properties from user defined InputStream.  systemPropertiesOverride should be used to override properties
 * using a given key to identify them from JVM args. (i.e. -Dkey.name to override the name property in the key file.)
 * TODO setup so the loading and overriding of properties is done for the user rather then them having to call it.
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class FreemarkerUtil {

    protected Configuration cfg;

    /**
     * Calls ftlWrite that also accepts a key, using the output getName as the key.
     * @param output
     * @param template
     * @return
     * @throws java.io.IOException
     * @throws freemarker.template.TemplateException
     */
    public File ftlWrite(File output, Template template, InputStream inputStream) throws IOException, TemplateException {

        return ftlWrite(output.getName(), output, template, inputStream);
    }

    /**
     * Loads properties from user defined properties file, if not available uses resource file
     *
     * writes processed template  to file
     * @param key
     * @param output
     * @param template
     * @throws IOException
     * @throws TemplateException
     */
    public static File ftlWrite(String key, File output, Template template, InputStream inputStream) throws IOException, TemplateException {
        Properties props = loadProperties(inputStream);
        systemPropertiesOverride(props, key);
        File outputFile = writeTemplateToFile(output, template, props);

        return outputFile;
    }

    protected static Properties loadProperties(InputStream inputStream) throws IOException {
        Properties props = new Properties();

        if(inputStream != null) {
            props.load(inputStream);
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
    protected static File writeTemplateToFile(File file, Template template, Properties props) throws IOException, TemplateException {
        String output = FreeMarkerTemplateUtils.processTemplateIntoString(template, props);
        FileUtils.writeStringToFile(file, output);

        return file;
    }

    /**
     * -Dkey.propertyname= to override the property value for propertyname.
     * @param props
     */
    public static void systemPropertiesOverride(Properties props, String key) {
        Enumeration<?> names = props.propertyNames();
        Object nameObject;
        String name;
        while (names.hasMoreElements()) {
            nameObject = names.nextElement();
            if (nameObject instanceof String) {
                name = (String)nameObject;
                props.setProperty(name, System.getProperty(key + "." + name, props.getProperty(name)));
            }
        }
    }

}
