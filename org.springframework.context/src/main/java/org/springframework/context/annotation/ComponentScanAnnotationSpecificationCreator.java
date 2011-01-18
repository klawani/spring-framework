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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.SpecificationCreator;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link SpecificationCreator} implementation that reads attributes from a
 * {@link ComponentScan @ComponentScan} annotation into a {@link ComponentScanSpecification}
 * which can in turn be executed by {@link ComponentScanExecutor}.
 * {@link ComponentScanElementSpecificationCreator} serves the same role for
 * the {@code <context:component-scan>} XML element.
 *
 * @author Chris Beams
 * @since 3.1
 * @see ComponentScan
 * @see ConfigurationClassBeanDefinitionReader
 * @see ComponentScanExecutor
 * @see ComponentScanElementSpecificationCreator
 */
class ComponentScanAnnotationSpecificationCreator implements AnnotationSpecificationCreator {

	private static final String BASE_PACKAGES_ATTRIBUTE = "basePackages";

	private static final String BASE_PACKAGES_ATTRIBUTE_ALIAS = "value";

	private static final String PACKAGE_OF_ATTRIBUTE = "packageOf";

	private static final String NAME_GENERATOR_ATTRIBUTE = "nameGenerator";

	private static final String SCOPE_RESOLVER_ATTRIBUTE = "scopeResolver";

	private static final String SCOPED_PROXY_ATTRIBUTE = "scopedProxy";

	private static final String RESOURCE_PATTERN_ATTRIBUTE = "resourcePattern";

	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "useDefaultFilters";

	private static final String EXCLUDE_FILTER_ATTRIBUTE = "excludeFilters";

	private static final String INCLUDE_FILTER_ATTRIBUTE = "includeFilters";

	private final ProblemReporter problemReporter;


	public ComponentScanAnnotationSpecificationCreator(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	/**
	 * Return whether the given metadata includes {@link ComponentScan} information.
	 */
	public boolean accepts(AnnotationMetadata metadata) {
		return metadata.hasAnnotation(ComponentScan.class.getName());
	}

	/**
	 * Create and return a new {@link ComponentScanSpecification} from the given
	 * {@link ComponentScan} annotation metadata.
	 * @throws IllegalArgumentException if ComponentScan attributes are not present in metadata
	 * @see #accepts(AnnotationMetadata)
	 */
	public ComponentScanSpecification createFrom(AnnotationMetadata metadata) {
		Map<String, Object> componentScanAttributes =
			metadata.getAnnotationAttributes(ComponentScan.class.getName(), true);

		Assert.notNull(componentScanAttributes,
				String.format("ComponentScan annotation not found while parsing " +
						"metadata for class [%s]. Use accepts(metadata) before " +
						"calling parse(metadata)", metadata.getClassName()));

		ComponentScanSpecification spec = new ComponentScanSpecification();

		String[] basePackageClasses = (String[])componentScanAttributes.get(PACKAGE_OF_ATTRIBUTE);
		String[] basePackages = (String[])componentScanAttributes.get(BASE_PACKAGES_ATTRIBUTE);
		String[] basePackagesAlias = (String[])componentScanAttributes.get(BASE_PACKAGES_ATTRIBUTE_ALIAS);
		if ((basePackages.length > 0 && basePackagesAlias.length > 0)
				|| basePackageClasses.length == 0 && basePackages.length == 0 && basePackagesAlias.length == 0) {
			this.problemReporter.fatal(new InvalidComponentScanProblem(metadata.getClassName()));
		}
		for (String className : basePackageClasses) {
			spec.addBasePackage(className.substring(0, className.lastIndexOf('.')));
		}
		for (String pkg : basePackages) {
			spec.addBasePackage(pkg);
		}
		for (String pkg : basePackagesAlias) {
			spec.addBasePackage(pkg);
		}

		ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
		spec.resourcePattern((String)componentScanAttributes.get(RESOURCE_PATTERN_ATTRIBUTE));
		spec.useDefaultFilters((Boolean)componentScanAttributes.get(USE_DEFAULT_FILTERS_ATTRIBUTE));
		spec.beanNameGenerator(instantiateUserDefinedStrategy(
				(String)componentScanAttributes.get(NAME_GENERATOR_ATTRIBUTE), BeanNameGenerator.class, classLoader));
		spec.scopeMetadataResolver(instantiateUserDefinedStrategy(
				(String)componentScanAttributes.get(SCOPE_RESOLVER_ATTRIBUTE), ScopeMetadataResolver.class, classLoader));
		ScopedProxyMode scopedProxyMode = (ScopedProxyMode) componentScanAttributes.get(SCOPED_PROXY_ATTRIBUTE);
		if (scopedProxyMode != ScopedProxyMode.DEFAULT) {
			spec.scopedProxyMode(scopedProxyMode);
		}
		Filter[] includeFilters = (Filter[]) componentScanAttributes.get(INCLUDE_FILTER_ATTRIBUTE);
		for (Filter includeFilter : includeFilters) {
			spec.addIncludeFilter(createTypeFilter(includeFilter));
		}
		Filter[] excludeFilters = (Filter[]) componentScanAttributes.get(EXCLUDE_FILTER_ATTRIBUTE);
		for (Filter excludeFilter : excludeFilters) {
			spec.addExcludeFilter(createTypeFilter(excludeFilter));
		}

		return spec;
	}

	@SuppressWarnings("unchecked")
	protected TypeFilter createTypeFilter(Filter filter) {
		Class<?> clazz = filter.value();
		FilterType filterType = filter.type();
		switch (filterType) {
			case ANNOTATION:
				return new AnnotationTypeFilter((Class<? extends Annotation>)clazz);
			case ASSIGNABLE_TYPE:
				return new AssignableTypeFilter(clazz);
			case CUSTOM:
				return BeanUtils.instantiateClass(clazz, TypeFilter.class);
			default:
				throw new IllegalArgumentException("Unknown FilterType: " + filterType);
		}
	}

	/**
	 * TODO SPR-7194: duplicated from {@link ComponentScanBeanDefinitionParser}
	 */
	@SuppressWarnings("unchecked")
	private <T> T instantiateUserDefinedStrategy(String className, Class<T> strategyType, ClassLoader classLoader) {
		Object result = null;
		try {
			result = classLoader.loadClass(className).newInstance();
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Class [" + className + "] for strategy [" +
					strategyType.getName() + "] not found", ex);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Unable to instantiate class [" + className + "] for strategy [" +
					strategyType.getName() + "]. A zero-argument constructor is required", ex);
		}

		if (!strategyType.isAssignableFrom(result.getClass())) {
			throw new IllegalArgumentException("Provided class name must be an implementation of " + strategyType);
		}
		return (T)result;
	}


	private static class InvalidComponentScanProblem extends Problem {
		public InvalidComponentScanProblem(String className) {
			super("@ComponentScan must declare either 'basePackages' or 'packageOf' attributes; " +
					"'value' may be used as an alias for 'basePackages' but they must not be used " +
					"at the same time.",
					new Location(new DescriptiveResource(className)));
		}
	}
}
