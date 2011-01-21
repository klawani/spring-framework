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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.FeatureSpecification;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Feature;
import org.springframework.context.annotation.ImportResource;
import org.springframework.util.Assert;

import test.beans.Colour;
import test.beans.TestBean;

/**
 * Much like the @Value issues described in {@link FeatureMethodAndValueInjectionTests},
 * autowired fields are also not processed for early-instantiated @Configuration classes.
 * Two tests below fail to demonstrate the problem.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class FeatureMethodAndAutowiredFieldTests {

	/**
	 * Passing test providing a control that @Autowired fields are injected as
	 * expected as long as there are no @Feature methods present.
	 */
	@Test
	public void autowiredInjectionOccursInConfigClassesWithoutFeatureMethods() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ConfigWithAutowiredField.class);
		ctx.register(ColourHolder.class);
		ctx.refresh();

		assertThat(ctx.getBean(TestBean.class).getFavouriteColour(), equalTo(Colour.BLUE));
	}

	/**
	 * Fails.
	 */
	@Test
	public void autowiredInjectionOccursInConfigClassesWithFeatureMethods() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ConfigWithFeatureMethodAndAutowiredField.class);
		ctx.register(ColourHolder.class);
		ctx.refresh();

		assertThat(ctx.getBean(TestBean.class).getFavouriteColour(), equalTo(Colour.BLUE));
	}

	/**
	 * Fails.
	 */
	@Test
	public void autowiredInjectionFromXmlOccursInConfigClassesWithFeatureMethods() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ConfigWithFeatureMethodAndAutowiredFieldFromXml.class);
		ctx.refresh();

		assertThat(ctx.getBean(TestBean.class).getFavouriteColour(), equalTo(Colour.BLUE));
	}

}


@Configuration
class ConfigWithAutowiredField {

	@Autowired
	private ColourHolder colourHolder;

	@Bean
	public TestBean testBean() {
		TestBean testBean = new TestBean();
		Assert.notNull(this.colourHolder, "ColourHolder bean was not injected.");
		testBean.setFavouriteColour(this.colourHolder.color);
		return testBean;
	}
}


@Configuration
class ConfigWithFeatureMethodAndAutowiredField {

	@Autowired
	private ColourHolder colourHolder;

	@Feature
	public FeatureSpecification spec() {
		return new StubSpecification();
	}

	@Bean
	public TestBean testBean() {
		TestBean testBean = new TestBean();
		Assert.notNull(this.colourHolder, "ColourHolder bean was not injected.");
		testBean.setFavouriteColour(this.colourHolder.color);
		return testBean;
	}
}


@Configuration
@ImportResource("org/springframework/context/annotation/configuration/FeatureMethodAndAutowiredFieldTests.xml")
class ConfigWithFeatureMethodAndAutowiredFieldFromXml {
	@Autowired
	private ColourHolder colourHolder;

	@Feature
	public FeatureSpecification spec() {
		return new StubSpecification();
	}

	@Bean
	public TestBean testBean() {
		TestBean testBean = new TestBean();
		Assert.notNull(this.colourHolder, "ColourHolder bean was not injected.");
		testBean.setFavouriteColour(this.colourHolder.color);
		return testBean;
	}
}

class ColourHolder {
	Colour color = Colour.BLUE;
}