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

import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ExecutorContext;
import org.springframework.context.FeatureSpecification;
import org.springframework.context.InvalidSpecificationException;
import org.springframework.context.SpecificationExecutor;

import test.beans.ITestBean;

/**
 * Tests that bean methods referenced from within Feature methods
 * get proxied early.
 * 
 * @author Chris Beams
 */
public class FeatureMethodEarlyBeanProxyTests {
	@Test
	public void test() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(FeatureConfig.class);

		ITestBean heldBean = ctx.getBean(BeanHolder.class).bean;
		ITestBean actualBean = ctx.getBean(ITestBean.class);

		assertThat(
				"the object used by the Feature method should be a proxy",
				AopUtils.isAopProxy(heldBean), is(true));

		assertThat(
				"the bean itself should not be a proxy",
				AopUtils.isAopProxy(actualBean), is(false));
	}
}

@Configuration
class FeatureConfig {
	@Feature
	public DummySpecification feature() {
		return new DummySpecification(myBean());
	}

	@Bean
	public ITestBean myBean() {
		return new test.beans.TestBean();
	}
}

class DummySpecification implements FeatureSpecification {

	private final ITestBean bean;

	public DummySpecification(ITestBean bean) {
		this.bean = bean;
	}

	public void validate() throws InvalidSpecificationException {
		// TODO Auto-generated method stub
		
	}

	public Class<? extends SpecificationExecutor> getExecutorType() {
		return DummyExecutor.class;
	}

	public ITestBean getBean() {
		return this.bean;
	}

}

class DummyExecutor implements SpecificationExecutor {

	public boolean accepts(FeatureSpecification spec) {
		return true;
	}

	public void execute(FeatureSpecification spec, ExecutorContext executorContext) {
		DummySpecification dummySpec = (DummySpecification)spec;
		BeanHolder beanHolder = new BeanHolder(dummySpec.getBean());
		((DefaultSingletonBeanRegistry)executorContext.getRegistry()).registerSingleton("beanHolder", beanHolder);
	}
	
}

class BeanHolder {

	final ITestBean bean;

	public BeanHolder(ITestBean bean) {
		this.bean = bean;
	}
	
}
