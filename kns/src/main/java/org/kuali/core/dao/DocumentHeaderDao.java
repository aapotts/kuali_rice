/*
 * Copyright 2005-2007 The Kuali Foundation.
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
package org.kuali.core.dao;

import java.sql.Date;
import java.util.Collection;

import org.kuali.core.bo.DocumentHeader;

/**
 * This is the data access interface for DocumentHeader objects.
 * 
 * 
 */
public interface DocumentHeaderDao {
    public DocumentHeader getByDocumentHeaderId(String id);

    /**
     * @param id
     * @return documentHeader of the document which corrects the document with the given documentId
     */
    public DocumentHeader getCorrectingDocumentHeader(String documentId);

    /**
     * Retrieves a collection of DocumentHeaders that were finalized on a given date
     * 
     * @param documentFinalDate
     */
    public Collection getByDocumentFinalDate(Date documentFinalDate);
}
