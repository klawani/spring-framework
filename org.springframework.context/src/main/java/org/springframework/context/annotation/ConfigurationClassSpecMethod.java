/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.context.annotation;

import org.springframework.core.type.MethodMetadata;

/**
 * Represents a {@link Configuration} class method marked with the {@link SpecMethod} annotation.
 *
 * @author Chris Beams
 * @since 3.1
 */
class ConfigurationClassSpecMethod {

	private final MethodMetadata metadata;

	private final ConfigurationClass configurationClass;

	public ConfigurationClassSpecMethod(MethodMetadata metadata, ConfigurationClass configurationClass) {
		this.metadata = metadata;
		this.configurationClass = configurationClass;
	}

	public MethodMetadata getMetadata() {
		return this.metadata;
	}
}
