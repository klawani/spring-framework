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

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.AbstractSpecificationExecutor;
import org.springframework.context.ExecutorContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.TypeFilter;

/**
 * TODO SPR-7194: document
 * 
 * @author Chris Beams
 * @since 3.1
 */
class ComponentScanSpecificationExecutor extends AbstractSpecificationExecutor<ComponentScanSpecification> {

	private BeanDefinitionDefaults beanDefinitionDefaults;
	private String[] autowireCandidatePatterns;
	private Set<BeanDefinitionHolder> scannedBeans;


	public void setBeanDefinitionDefaults(BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults = beanDefinitionDefaults;
	}

	public BeanDefinitionDefaults getBeanDefinitionDefaults() {
		return this.beanDefinitionDefaults;
	}

	public void setAutowireCandidatePatterns(String[] autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}

	public String[] getAutowireCandidatePatterns() {
		return this.autowireCandidatePatterns;
	}

	/**
	 * Return the bean definitions scanned on the last call to
	 * {@link #execute(ComponentScanSpecification)} or {@code null} if {@code execute} has
	 * not yet been called.
	 */
	public Set<BeanDefinitionHolder> getScannedBeans() {
		return this.scannedBeans;
	}

	/**
	 * Configure a {@link ClassPathBeanDefinitionScanner} based on the content of
	 * the given specification and perform actual scanning and bean definition
	 * registration.
	 */
	public void doExecute(ComponentScanSpecification spec, ExecutorContext executorContext) {
		BeanDefinitionRegistry registry = executorContext.getRegistry();
		ResourceLoader resourceLoader = executorContext.getResourceLoader();
		Environment environment = executorContext.getEnvironment();

		ClassPathBeanDefinitionScanner scanner = spec.getUseDefaultFilters() == null ?
			new ClassPathBeanDefinitionScanner(registry) :
			new ClassPathBeanDefinitionScanner(registry, spec.getUseDefaultFilters());

		scanner.setResourceLoader(resourceLoader);
		scanner.setEnvironment(environment);

		if (this.beanDefinitionDefaults != null) {
			scanner.setBeanDefinitionDefaults(this.beanDefinitionDefaults);
		}
		if (this.autowireCandidatePatterns != null) {
			scanner.setAutowireCandidatePatterns(this.autowireCandidatePatterns);
		}

		if (spec.getResourcePattern() != null) {
			scanner.setResourcePattern(spec.getResourcePattern());
		}
		if (spec.getBeanNameGenerator() != null) {
			scanner.setBeanNameGenerator(spec.getBeanNameGenerator());
		}
		if (spec.getIncludeAnnotationConfig() != null) {
			scanner.setIncludeAnnotationConfig(spec.getIncludeAnnotationConfig());
		}
		if (spec.getScopeMetadataResolver() != null) {
			scanner.setScopeMetadataResolver(spec.getScopeMetadataResolver());
		}
		if (spec.getScopedProxyMode() != null) {
			scanner.setScopedProxyMode(spec.getScopedProxyMode());
		}
		if (spec.getIncludeFilters() != null) {
			for (TypeFilter filter : spec.getIncludeFilters()) {
				scanner.addIncludeFilter(filter);
			}
		}
		if (spec.getExcludeFilters() != null) {
			for (TypeFilter filter : spec.getExcludeFilters()) {
				scanner.addExcludeFilter(filter);
			}
		}

		this.scannedBeans = scanner.doScan(spec.getBasePackages());
	}

}