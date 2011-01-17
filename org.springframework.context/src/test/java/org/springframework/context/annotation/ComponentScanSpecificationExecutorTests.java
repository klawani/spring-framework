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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ExecutorContext;
import org.springframework.context.InvalidSpecificationException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;

/**
 * Unit tests for {@link ComponentScanSpecificationExecutor}.
 * 
 * @author Chris Beams
 */
public class ComponentScanSpecificationExecutorTests {
	private ComponentScanSpecificationExecutor executor;
	private ExecutorContext executorContext;

	@Before
	public void setUp() {
		this.executor = new ComponentScanSpecificationExecutor();
		this.executorContext = new ExecutorContext();
		this.executorContext.setRegistry(new DefaultListableBeanFactory());
		this.executorContext.setResourceLoader(new DefaultResourceLoader());
		this.executorContext.setEnvironment(new MockEnvironment());
	}

	@Test
	public void validSpec() {
		ComponentScanSpecification spec = new ComponentScanSpecification();
		spec.addBasePackage("example.scannable");

		this.executor.execute(spec, this.executorContext);

		boolean targetBeanFound = false;
		for (BeanDefinitionHolder beanDefHolder : this.executor.getScannedBeans()) {
			if (beanDefHolder.getBeanName().equals("fooServiceImpl")) {
				targetBeanFound = true;
				break;
			}
		}
		assertThat(targetBeanFound, is(true));
	}

	@Test(expected=InvalidSpecificationException.class)
	public void invalidSpec() {
		ComponentScanSpecification spec = new ComponentScanSpecification();
		this.executor.execute(spec, this.executorContext);
	}

}
