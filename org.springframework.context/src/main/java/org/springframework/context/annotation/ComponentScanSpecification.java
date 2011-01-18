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

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.InvalidSpecificationException;
import org.springframework.context.FeatureSpecification;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link FeatureSpecification} implementation that holds component-scanning
 * configuration metadata.  This decouples the metadata from its XML or
 * annotation source. Once this structure has been populated by an XML
 * or annotation parser, it may be read by {@link ComponentScanSpecificationExecutor}
 * which is responsible for actual scanning and bean definition registration.
 *
 * @author Chris Beams
 * @since 3.1
 * @see ComponentScan
 * @see ComponentScanAnnotationSpecificationCreator
 * @see ComponentScanElementSpecificationCreator
 * @see ComponentScanSpecificationExecutor
 */
class ComponentScanSpecification implements FeatureSpecification {

	private Boolean includeAnnotationConfig = null;
	private String resourcePattern = null;
	private String[] basePackages = null;
	private Boolean useDefaultFilters = null;
	private BeanNameGenerator beanNameGenerator = null;
	private ScopeMetadataResolver scopeMetadataResolver = null;
	private ScopedProxyMode scopedProxyMode = null;
	private TypeFilter[] includeFilters = null;
	private TypeFilter[] excludeFilters = null;

	public void validate() throws InvalidSpecificationException {
		if(ObjectUtils.isEmpty(this.basePackages)) {
			throw new InvalidSpecificationException("At least one base package must be specified");
		}
	}

	public Boolean getIncludeAnnotationConfig() {
		return this.includeAnnotationConfig;
	}

	public void setIncludeAnnotationConfig(Boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}

	public void setResourcePattern(String resourcePattern) {
		this.resourcePattern = resourcePattern;
	}

	public String getResourcePattern() {
		return resourcePattern;
	}

	public void addBasePackage(String basePackage) {
		this.basePackages = (this.basePackages == null) ?
			new String[] { basePackage } :
			StringUtils.addStringToArray(this.basePackages, basePackage);
	}

	public void setBasePackages(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		this.basePackages = basePackages;
	}

	public String[] getBasePackages() {
		return basePackages;
	}

	public void setUseDefaultFilters(Boolean useDefaultFilters) {
		this.useDefaultFilters = useDefaultFilters;
	}

	public Boolean getUseDefaultFilters() {
		return this.useDefaultFilters;
	}

	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	public BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}

	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
	}

	public ScopeMetadataResolver getScopeMetadataResolver() {
		return this.scopeMetadataResolver;
	}

	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopedProxyMode = scopedProxyMode;
	}

	public ScopedProxyMode getScopedProxyMode() {
		return this.scopedProxyMode;
	}

	public void addIncludeFilter(TypeFilter typeFilter) {
		this.includeFilters = (this.includeFilters == null) ?
			new TypeFilter[] { typeFilter } :
			ObjectUtils.addObjectToArray(this.includeFilters, typeFilter);
	}

	public TypeFilter[] getIncludeFilters() {
		return this.includeFilters;
	}

	public void addExcludeFilter(TypeFilter typeFilter) {
		this.excludeFilters = (this.excludeFilters == null) ?
			new TypeFilter[] { typeFilter } :
			ObjectUtils.addObjectToArray(this.excludeFilters, typeFilter);
	}

	public TypeFilter[] getExcludeFilters() {
		return this.excludeFilters;
	}

}
