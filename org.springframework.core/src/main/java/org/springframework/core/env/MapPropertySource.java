/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.env;

import java.util.Map;


/**
 * TODO SPR-7508: document
 *
 * Consider adding a TypeConvertingMapPropertySource to accommodate
 * non-string keys and values. Could be confusing when used in conjunction
 * with Environment.getProperty(), which also does type conversions. If this
 * is added, consider renaming this class to SimpleMapPropertySource and
 * rename PropertiesPropertySource to SimplePropertiesPropertySource.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class MapPropertySource extends PropertySource<Map<String, String>> {

	protected MapPropertySource(String name, Map<String, String> source) {
		super(name, source);
	}

	public boolean containsProperty(String key) {
		return this.getSource().containsKey(key);
	}

	public String getProperty(String key) {
		return this.getSource().get(key);
	}

	@Override
	public int size() {
		return this.getSource().size();
	}

}