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
package org.kuali.core.exceptions;

/**
 * Exception thrown when the persistence layer experiences a problem accessing database, table, or column metadata.
 * 
 * 
 */
/**
 * This class represents an exception that is thrown when the persistence layer experiences a problem accessing database, table, or
 * column metadata.
 * 
 * 
 */
public class MetadataException extends RuntimeException {

    private static final long serialVersionUID = -3817586611969869945L;

    /**
     * @param message
     */
    public MetadataException(String message) {
        super(message);
    }

    /**
     * @param message
     */
    public MetadataException(String message, Throwable t) {
        super(message, t);
    }
}
