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

package org.springframework.transaction.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Feature;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CallCountingTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean;
import org.springframework.transaction.config.TxAnnotationDriven;

/**
 * Integration tests for {@link TxAnnotationDriven} support within @Configuration
 * classes. Adapted from original tx: namespace tests at
 * {@link AnnotationTransactionNamespaceHandlerTests}.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class TxAnnotationDrivenFeatureTests {
	@Test
	public void isProxy() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TxConfig.class);
		TransactionalTestBean bean = ctx.getBean(TransactionalTestBean.class);
		assertThat("testBean is not a proxy", AopUtils.isAopProxy(bean), is(true));
		Map<?,?> services = ctx.getBeansWithAnnotation(Service.class);
		assertThat("Stereotype annotation not visible", services.containsKey("testBean"), is(true));
	}
}

@Configuration
class TxConfig {

	@Feature
	public TxAnnotationDriven tx() {
		return new TxAnnotationDriven(this.txManager()).proxyTargetClass(false);
	}

	@Bean
	public TransactionalTestBean testBean() {
		return new TransactionalTestBean();
	}

	@Bean
	public PlatformTransactionManager txManager() {
		return new CallCountingTransactionManager();
	}

}