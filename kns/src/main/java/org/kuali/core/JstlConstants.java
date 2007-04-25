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
package org.kuali.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to expose Java constants to jstl as a HashMap. Have constants classes extend this class to expose them to jstl
 * as a HashMap
 * 
 * 
 */
public class JstlConstants extends HashMap {
    private static final long serialVersionUID = 959692943635271761L;
    private boolean initialised = false;

    public JstlConstants() {
        publishFields(this, this.getClass());
        initialised = true;
    }

    /**
     * Publishes all of the static, final, non-private fields of the given Class as entries in the given HashMap instance
     * 
     * @param constantMap
     * @param c
     */
    protected void publishFields(Map constantMap, Class c) {
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            int modifier = field.getModifiers();

            // publish values of static, final, non-private members
            if (Modifier.isStatic(modifier) && Modifier.isFinal(modifier) && !Modifier.isPrivate(modifier)) {
                try {
                    String fieldName = field.getName();

                    constantMap.put(fieldName, field.get(null));
                }
                catch (IllegalAccessException e) {
                }
            }
        }

        // publish values of appropriate fields of member classes
        publishMemberClassFields(constantMap, c);
    }

    /**
     * Publishes all of the static, final, non-private fields of the non-anonymous member classes of the given Class as entries in
     * the given HashMap instance
     * 
     * @param constantMap
     * @param c
     */
    protected void publishMemberClassFields(Map constantMap, Class c) {
        Class[] memberClasses = c.getClasses();

        for (Class memberClass : memberClasses) {
            if (!memberClass.isAnonymousClass()) {
                String memberPrefix = memberClass.getSimpleName();

                Map subclassMap = new HashMap();
                publishFields(subclassMap, memberClass);
                constantMap.put(memberClass.getSimpleName(), subclassMap);
            }
        }
    }


    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        if (!initialised)
            super.clear();
        else
            throw new UnsupportedOperationException("Cannot modify this map");
    }


    @Override
    public Object put(Object key, Object value) {
        if (!initialised)
            return super.put(key, value);
        else
            throw new UnsupportedOperationException("Cannot modify this map");
    }

    @Override
    public void putAll(Map m) {
        if (!initialised)
            super.putAll(m);
        else
            throw new UnsupportedOperationException("Cannot modify this map");
    }

    @Override
    public Object remove(Object key) {
        if (!initialised)
            return super.remove(key);
        else
            throw new UnsupportedOperationException("Cannot modify this map");
    }
}