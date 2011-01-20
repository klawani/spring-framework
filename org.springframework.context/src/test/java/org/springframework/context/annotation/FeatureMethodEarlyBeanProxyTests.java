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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ExecutorContext;
import org.springframework.context.FeatureSpecification;
import org.springframework.context.InvalidSpecificationException;
import org.springframework.context.SpecificationExecutor;

import test.beans.ITestBean;
import test.beans.TestBean;

/**
 * Tests that bean methods referenced from within Feature methods
 * get proxied early.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class FeatureMethodEarlyBeanProxyTests {

	@Test
	public void earlyProxyCreationAndBeanRegistrationLifecycle() {
		AnnotationConfigApplicationContext ctx =
			new AnnotationConfigApplicationContext(FeatureConfig.class);

		//
		// see additional assertions in FeatureConfig#feature()
		//

		// sanity check that all the bean definitions we expect are present
		assertThat(ctx.getBeanFactory().containsBeanDefinition("lazyHelperBean"), is(true));
		assertThat(ctx.getBeanFactory().containsBeanDefinition("eagerHelperBean"), is(true));
		assertThat(ctx.getBeanFactory().containsBeanDefinition("lazyPassthroughBean"), is(true));
		assertThat(ctx.getBeanFactory().containsBeanDefinition("eagerPassthroughBean"), is(true));


		// the lazy helper bean had methods invoked during feature method execution. it should be registered
		assertThat(ctx.getBeanFactory().containsSingleton("lazyHelperBean"), is(true));

		// the eager helper bean had methods invoked but should be registered in any case is it is non-lazy
		assertThat(ctx.getBeanFactory().containsSingleton("eagerHelperBean"), is(true));

		// the lazy passthrough bean was referenced in the feature method, but never invoked. it should not be registered
		assertThat(ctx.getBeanFactory().containsSingleton("lazyPassthroughBean"), is(false));

		// the eager passthrough bean should be registered in any case as it is non-lazy
		assertThat(ctx.getBeanFactory().containsSingleton("eagerPassthroughBean"), is(true));


		// now actually fetch all the beans. none should be proxies
		assertThat(Proxy.isProxyClass(ctx.getBean("lazyHelperBean").getClass()), is(false));
		assertThat(Proxy.isProxyClass(ctx.getBean("eagerHelperBean").getClass()), is(false));
		assertThat(Proxy.isProxyClass(ctx.getBean("lazyPassthroughBean").getClass()), is(false));
		assertThat(Proxy.isProxyClass(ctx.getBean("eagerPassthroughBean").getClass()), is(false));
	}


	@Test
	public void earlyProxyBeansMustBeInterfaceBased() {
		try {
			new AnnotationConfigApplicationContext(InvalidFeatureConfig.class);
			fail("should have thrown");
		} catch (FeatureMethodExecutionException ex) {
			assertThat(ex.getCause(), instanceOf(InvocationTargetException.class));
			assertThat(ex.getCause().getCause(), instanceOf(ProxyCreationException.class));
		}
	}
}


@Configuration
class FeatureConfig implements BeanFactoryAware {
	private DefaultListableBeanFactory beanFactory;

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory)beanFactory;
	}

	@Feature
	public DummySpecification feature() {
		DummySpecification spec = new DummySpecification();

		// invocation of @Bean methods within @Feature methods should return proxies
		ITestBean lazyHelperBean = this.lazyHelperBean();
		ITestBean eagerHelperBean = this.eagerHelperBean();
		ITestBean lazyPassthroughBean = this.lazyPassthroughBean();
		ITestBean eagerPassthroughBean = this.eagerPassthroughBean();

		assertThat(Proxy.isProxyClass(lazyHelperBean.getClass()), is(true));
		assertThat(Proxy.isProxyClass(eagerHelperBean.getClass()), is(true));
		assertThat(Proxy.isProxyClass(lazyPassthroughBean.getClass()), is(true));
		assertThat(Proxy.isProxyClass(eagerPassthroughBean.getClass()), is(true));

		// but at this point, the proxy instances should not have
		// been registered as singletons with the container.
		assertThat(this.beanFactory.containsSingleton("lazyHelperBean"), is(false));
		assertThat(this.beanFactory.containsSingleton("eagerHelperBean"), is(false));
		assertThat(this.beanFactory.containsSingleton("lazyPassthroughBean"), is(false));
		assertThat(this.beanFactory.containsSingleton("eagerPassthroughBean"), is(false));

		// invoking a method on the proxy should cause it to pass through
		// to the container, instantiate the actual bean in question and
		// register that actual underlying instance as a singleton.
		assertThat(lazyHelperBean.getName(), equalTo("lazyHelper"));
		assertThat(eagerHelperBean.getName(), equalTo("eagerHelper"));

		assertThat(this.beanFactory.containsSingleton("lazyHelperBean"), is(true));
		assertThat(this.beanFactory.containsSingleton("eagerHelperBean"), is(true));

		// since no methods were called on the passthrough beans, they should remain
		// uncreated / unregistered.
		assertThat(this.beanFactory.containsSingleton("lazyPassthroughBean"), is(false));
		assertThat(this.beanFactory.containsSingleton("eagerPassthroughBean"), is(false));

		return spec;
	}

	@Lazy @Bean
	public ITestBean lazyHelperBean() {
		return new TestBean("lazyHelper");
	}

	@Bean
	public ITestBean eagerHelperBean() {
		return new TestBean("eagerHelper");
	}

	@Lazy @Bean
	public ITestBean lazyPassthroughBean() {
		return new TestBean("lazyPassthrough");
	}

	@Bean
	public ITestBean eagerPassthroughBean() {
		return new TestBean("eagerPassthrough");
	}

}

class DummySpecification implements FeatureSpecification {

	public void validate() throws InvalidSpecificationException {
	}

	public Class<? extends SpecificationExecutor> getExecutorType() {
		return DummyExecutor.class;
	}

}

class DummyExecutor implements SpecificationExecutor {

	public boolean accepts(FeatureSpecification spec) {
		return true;
	}

	public void execute(FeatureSpecification spec, ExecutorContext executorContext) {
		// no-op
	}

}


@Configuration
class InvalidFeatureConfig {
	@Feature
	public FeatureSpecification feature() throws Throwable {
		try {
			// this will cause the exception as TestBean return type is not an interface
			this.testBean();
		} catch (ProxyCreationException ex) {
			assertThat(ex.getMessage().startsWith("@Bean method"), is(true));
			throw ex;
		}
		throw new AssertionError("should have thrown");
	}

	@Bean
	public TestBean testBean() {
		return new TestBean("invalid");
	}
}

