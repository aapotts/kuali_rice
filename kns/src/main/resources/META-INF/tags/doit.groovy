println """<?xml version="1.0" encoding="UTF-8" ?>
<!--
	Copyright 2005-2006 The Kuali Foundation.
	
	Licensed under the Educational Community License, Version 1.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	http://www.opensource.org/licenses/ecl1.php
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
-->
<taglib xmlns="http://java.sun.com/xml/ns/j2ee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee web-jsptaglibrary_2_0.xsd"
	version="2.0">

	<description>JSP 2.0 tag files</description>
	<tlib-version>1.0</tlib-version>
	<short-name>Rice Tags</short-name>
	<uri>http://rice.kuali.org/tagfiles/core</uri>"""

new File('.').eachFile { 
	if (it.name =~ /.*\.tag/) {
		print """
	<tag-file>
		<name>${it.name[0..-5]}</name>
		<path>/META-INF/tags/${it.path[2..-1]}</path>
	</tag-file>"""
	}
}

println ""
println "</taglib>"
