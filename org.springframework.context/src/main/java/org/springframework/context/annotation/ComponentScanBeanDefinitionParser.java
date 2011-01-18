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
import java.util.regex.Pattern;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ExecutorContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for the &lt;context:component-scan/&gt; element. Parsed metadata is
 * used to populate a {@link ComponentScanSpecification} object which is in turn
 * delegated to a {@link ComponentScanExecutor} for actual scanning and
 * bean definition registration.
 * 
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see ComponentScan
 * @see ComponentScanSpecification
 * @see ComponentScanExecutor
 */
public class ComponentScanBeanDefinitionParser implements BeanDefinitionParser {

	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";

	private static final String RESOURCE_PATTERN_ATTRIBUTE = "resource-pattern";

	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "use-default-filters";

	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";

	private static final String NAME_GENERATOR_ATTRIBUTE = "name-generator";

	private static final String SCOPE_RESOLVER_ATTRIBUTE = "scope-resolver";

	private static final String SCOPED_PROXY_ATTRIBUTE = "scoped-proxy";

	private static final String EXCLUDE_FILTER_ELEMENT = "exclude-filter";

	private static final String INCLUDE_FILTER_ELEMENT = "include-filter";

	private static final String FILTER_TYPE_ATTRIBUTE = "type";

	private static final String FILTER_EXPRESSION_ATTRIBUTE = "expression";


	public BeanDefinition parse(Element element, ParserContext parserContext) {
		ComponentScanSpecification spec = createSpecification(element, parserContext);

		ComponentScanExecutor specExecutor = new ComponentScanExecutor();
		specExecutor.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());
		specExecutor.setAutowireCandidatePatterns(parserContext.getDelegate().getAutowireCandidatePatterns());

		specExecutor.execute(spec, createExecutorContext(parserContext));

		return null;
	}

	protected ComponentScanSpecification createSpecification(Element element, ParserContext parserContext) {
		XmlReaderContext readerContext = parserContext.getReaderContext();

		ComponentScanSpecification spec = ComponentScanSpecification.withBasePackages(
				StringUtils.tokenizeToStringArray(element.getAttribute(BASE_PACKAGE_ATTRIBUTE),
						ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));

		spec.setSource(readerContext.extractSource(element));
		spec.setSourceName(element.getTagName());

		if (element.hasAttribute(ANNOTATION_CONFIG_ATTRIBUTE)) {
			spec.includeAnnotationConfig(Boolean.valueOf(element.getAttribute(ANNOTATION_CONFIG_ATTRIBUTE)));
		}

		if (element.hasAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE)) {
			spec.useDefaultFilters(Boolean.valueOf(element.getAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE)));
		}

		if (element.hasAttribute(RESOURCE_PATTERN_ATTRIBUTE)) {
			spec.resourcePattern(element.getAttribute(RESOURCE_PATTERN_ATTRIBUTE));
		}

		if (element.hasAttribute(NAME_GENERATOR_ATTRIBUTE)) {
			spec.beanNameGenerator(instantiateUserDefinedStrategy(
					element.getAttribute(NAME_GENERATOR_ATTRIBUTE), BeanNameGenerator.class,
					readerContext.getResourceLoader().getClassLoader()));
		}


		// Register ScopeMetadataResolver if class name provided.
		if (element.hasAttribute(SCOPE_RESOLVER_ATTRIBUTE)) {
			if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
				throw new IllegalArgumentException(
						"Cannot define both 'scope-resolver' and 'scoped-proxy' on <component-scan> tag");
			}
			ScopeMetadataResolver scopeMetadataResolver = instantiateUserDefinedStrategy(
					element.getAttribute(SCOPE_RESOLVER_ATTRIBUTE), ScopeMetadataResolver.class,
					readerContext.getResourceLoader().getClassLoader());
			spec.scopeMetadataResolver(scopeMetadataResolver);
		}

		if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
			String mode = element.getAttribute(SCOPED_PROXY_ATTRIBUTE);
			if ("targetClass".equals(mode)) {
				spec.scopedProxyMode(ScopedProxyMode.TARGET_CLASS);
			}
			else if ("interfaces".equals(mode)) {
				spec.scopedProxyMode(ScopedProxyMode.INTERFACES);
			}
			else if ("no".equals(mode)) {
				spec.scopedProxyMode(ScopedProxyMode.NO);
			}
			else {
				throw new IllegalArgumentException("scoped-proxy only supports 'no', 'interfaces' and 'targetClass'");
			}
		}

		// Parse exclude and include filter elements.
		ClassLoader classLoader = readerContext.getResourceLoader().getClassLoader();
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String localName = parserContext.getDelegate().getLocalName(node);
				try {
					if (INCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter((Element) node, classLoader);
						spec.addIncludeFilter(typeFilter);
					}
					else if (EXCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter((Element) node, classLoader);
						spec.addExcludeFilter(typeFilter);
					}
				}
				catch (Exception ex) {
					readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
				}
			}
		}

		return spec;
	}

	private ExecutorContext createExecutorContext(ParserContext parserContext) {
		ExecutorContext executorContext = new ExecutorContext();
		executorContext.setRegistry(parserContext.getRegistry());
		executorContext.setRegistrar(parserContext);
		executorContext.setResourceLoader(parserContext.getReaderContext().getResourceLoader());
		executorContext.setEnvironment(parserContext.getDelegate().getEnvironment());
		return executorContext;
	}

	@SuppressWarnings("unchecked")
	protected TypeFilter createTypeFilter(Element element, ClassLoader classLoader) {
		String filterType = element.getAttribute(FILTER_TYPE_ATTRIBUTE);
		String expression = element.getAttribute(FILTER_EXPRESSION_ATTRIBUTE);
		try {
			if ("annotation".equals(filterType)) {
				return new AnnotationTypeFilter((Class<Annotation>) classLoader.loadClass(expression));
			}
			else if ("assignable".equals(filterType)) {
				return new AssignableTypeFilter(classLoader.loadClass(expression));
			}
			else if ("aspectj".equals(filterType)) {
				return new AspectJTypeFilter(expression, classLoader);
			}
			else if ("regex".equals(filterType)) {
				return new RegexPatternTypeFilter(Pattern.compile(expression));
			}
			else if ("custom".equals(filterType)) {
				Class<?> filterClass = classLoader.loadClass(expression);
				if (!TypeFilter.class.isAssignableFrom(filterClass)) {
					throw new IllegalArgumentException(
							"Class is not assignable to [" + TypeFilter.class.getName() + "]: " + expression);
				}
				return (TypeFilter) BeanUtils.instantiateClass(filterClass);
			}
			else {
				throw new IllegalArgumentException("Unsupported filter type: " + filterType);
			}
		}
		catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Type filter class not found: " + expression, ex);
		}
	}

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

}
