/*
 * Copyright 2006-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License  distributed on an "AS " BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kuali.rice.config.at

import groovy.transform.Canonical

@Canonical class OutputAwareMvnContextImpl implements OutputAwareMvnContext {

    Properties projectProperties
    File workingDir
    File basedir
    String executable
    List<String> poms
    String pom
    boolean filterPom
    boolean quiet
    boolean silent
    List<String> args
    List<String> properties
    List<String> filterProperties
    boolean addEnvironment
    boolean addMavenOpts
    boolean failOnError
    boolean deleteTempPom
    Writer stdOutWriter
    Writer stdErrWriter
}
