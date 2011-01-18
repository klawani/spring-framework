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

package org.springframework.context;

public abstract class AbstractFeatureSpecification implements SourceAwareSpecification {

	private static final Object DUMMY_SOURCE = new Object();
	private static final String DUMMY_SOURCE_NAME = "dummySource";

	private Class<? extends SpecificationExecutor> executorType;

	private Object source = DUMMY_SOURCE;
	private String sourceName = DUMMY_SOURCE_NAME;

	protected AbstractFeatureSpecification(Class<? extends SpecificationExecutor> executorType) {
		this.executorType = executorType;
	}

	public Class<? extends SpecificationExecutor> getExecutorType() {
		return executorType;
	}

	public void setExecutorType(Class<? extends SpecificationExecutor> executorType) {
		this.executorType = executorType;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return this.source;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceName() {
		return this.sourceName;
	}

}
