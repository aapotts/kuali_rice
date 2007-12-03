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
package edu.iu.uis.eden.batch;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface describing an abstract XML document source collection
 * @see edu.iu.uis.eden.batch.XmlDoc
 * @see edu.iu.uis.eden.batch.BaseXmlDocCollection
 * @see edu.iu.uis.eden.batch.FileXmlDocCollection
 * @see edu.iu.uis.eden.batch.DirectoryXmlDocCollection
 * @see edu.iu.uis.eden.batch.ZipXmlDocCollection
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public interface XmlDocCollection {
    File getFile();
    List getXmlDocs();
    void close() throws IOException;
}