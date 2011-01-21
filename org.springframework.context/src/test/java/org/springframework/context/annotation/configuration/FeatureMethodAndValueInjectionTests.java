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

package org.springframework.context.annotation.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.FeatureSpecification;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.EarlyBeanReferenceProxy;
import org.springframework.context.annotation.Feature;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import test.beans.ITestBean;
import test.beans.TestBean;

/**
 * Configuration classes with {@link Feature} methods are instantiated early
 * in the container lifecycle, during the {@link BeanDefinitionRegistryPostProcessor} 
 * phase, and this is too early to receive @Value injection. These configuration
 * class instances must be specially processed later in the lifecycle to receive
 * value injection when it is available.
 *
 * It will always be the case that @Feature methods cannot reference @Value fields,
 * but other @Bean methods in these same configuration classes should be able to.
 * Remember that any @Bean methods referenced from @Feature methods will be proxied
 * using the {@link EarlyBeanReferenceProxy} approach; this means that when they are
 * actually invoked later, they should be able to the late-injected @Value fields.
 *
 * @author Chris Beams
 * @since 3.1
 * @see ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry
 */
public class FeatureMethodAndValueInjectionTests {

	/**
	 * This test fails due to @Feature method / @Value injection lifecycle issues
	 * described above.
	 */
	@Test
	public void valueFieldsAreLateInjected() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ConfigWithFeatureMethodAndValueField.class);
		ctx.register(PropertySourcesPlaceholderConfigurer.class);
		System.setProperty("test.name", "foo");
		ctx.refresh();
		System.clearProperty("test.name");

		TestBean testBean = ctx.getBean(TestBean.class);
		assertThat(testBean.getName(), equalTo("foo"));
	}
}

@Configuration
class ConfigWithFeatureMethodAndValueField {

	@Value("${test.name}")
	private String name;

	@Bean
	public ITestBean testBean() {
		return new TestBean(this.name);
	}

	@Feature
	public FeatureSpecification spec() {
		return new StubSpecification();
	}

}