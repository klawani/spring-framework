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

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ProxyType;
import org.springframework.transaction.CallCountingTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.config.TxAnnotationDriven;

public class AnnotationDrivenTxConfigurationClassTests {
	@Test
	public void test() {
		
	}
}

@Configuration
class TxConfig {

	/*
	@Specification
	public AnnotationDrivenTxSpecification txAnnotationDriven1() {
		AnnotationDrivenTxSpecification spec = new AnnotationDrivenTxSpecification();
		spec.setOrder(0);
		spec.setTransactionManager(txManager());
		spec.setProxyMode(ProxyMode.PROXY);
		spec.setProxyTargetClass(false);
		return spec;
	}
	*/

	@Specification
	public TxAnnotationDriven txAnnotationDriven2() {
		return new TxAnnotationDriven(this.txManager()).order(0).proxyType(ProxyType.SPRINGAOP).proxyTargetClass(false);
	}

	/*
	@Specification
	public AnnotationDrivenTxSpecification txAnnotationDriven3() {
		return new AnnotationDrivenTxSpecification().transactionManagerName("txManager").order(0).proxyType(ProxyType.SPRINGAOP).proxyTargetClass(false);
	}
	*/

	@Bean
	public PlatformTransactionManager txManager() {
		return new CallCountingTransactionManager();
	}

}

@interface Specification {
}