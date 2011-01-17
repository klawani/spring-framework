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

package org.springframework.transaction.config;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentRegistrar;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.AbstractSpecificationExecutor;
import org.springframework.context.ExecutorContext;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

class TxAnnotationDrivenSpecificationExecutor extends AbstractSpecificationExecutor<TxAnnotationDriven> {

	/**
	 * The bean name of the internally managed transaction advisor (used when
	 * proxyType == SPRINGAOP).
	 */
	public static final String TRANSACTION_ADVISOR_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAdvisor";

	/**
	 * The bean name of the internally managed transaction aspect (used when
	 * proxyType == ASPECTJ).
	 */
	public static final String TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAspect";

	private static final String TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.AnnotationTransactionAspect";


	@Override
	public void doExecute(TxAnnotationDriven txSpec, ExecutorContext executorContext) {
		BeanDefinitionRegistry registry = executorContext.getRegistry();
		ComponentRegistrar registrar = executorContext.getRegistrar();
		switch (txSpec.proxyType()) {
			case ASPECTJ:
				registerTransactionAspect(txSpec, registry, registrar);
				break;
			case SPRINGAOP:
				AopAutoProxyConfigurer.configureAutoProxyCreator(txSpec, registry, registrar);
				break;
			default:
				throw new IllegalArgumentException(
						String.format("proxy type %s is not supported", txSpec.proxyType()));
		}
	}

	private void registerTransactionAspect(TxAnnotationDriven spec, BeanDefinitionRegistry registry, ComponentRegistrar registrar) {
		if (!registry.containsBeanDefinition(TRANSACTION_ASPECT_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			def.setBeanClassName(TRANSACTION_ASPECT_CLASS_NAME);
			def.setFactoryMethodName("aspectOf");
			registerTransactionManager(spec, def);
			registrar.registerBeanComponent(new BeanComponentDefinition(def, TRANSACTION_ASPECT_BEAN_NAME));
		}
	}

	private static void registerTransactionManager(TxAnnotationDriven spec, BeanDefinition def) {
		def.getPropertyValues().add("transactionManagerBeanName", spec.transactionManagerName());
	}

	/**
	 * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
	 */
	private static class AopAutoProxyConfigurer {

		public static void configureAutoProxyCreator(TxAnnotationDriven txSpec, BeanDefinitionRegistry registry, ComponentRegistrar registrar) {
			Object source = txSpec.getSource();
			AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(registry, registrar, source, txSpec);

			if (!registry.containsBeanDefinition(TRANSACTION_ADVISOR_BEAN_NAME)) {

				// Create the TransactionAttributeSource definition.
				RootBeanDefinition sourceDef = new RootBeanDefinition(AnnotationTransactionAttributeSource.class);
				sourceDef.setSource(source);
				sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				String sourceName = registrar.registerWithGeneratedName(sourceDef);

				// Create the TransactionInterceptor definition.
				RootBeanDefinition interceptorDef = new RootBeanDefinition(TransactionInterceptor.class);
				interceptorDef.setSource(source);
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				registerTransactionManager(txSpec, interceptorDef);
				interceptorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
				String interceptorName = registrar.registerWithGeneratedName(interceptorDef);

				// Create the TransactionAttributeSourceAdvisor definition.
				RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryTransactionAttributeSourceAdvisor.class);
				advisorDef.setSource(source);
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
				advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
				if (txSpec.order() != null) {
					advisorDef.getPropertyValues().add("order", txSpec.order());
				}
				registry.registerBeanDefinition(TRANSACTION_ADVISOR_BEAN_NAME, advisorDef);

				CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(txSpec.getSourceName(), source);
				compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, TRANSACTION_ADVISOR_BEAN_NAME));
				registrar.registerComponent(compositeDef);
			}
		}
	}
}
