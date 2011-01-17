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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ExecutorContext;
import org.w3c.dom.Element;

/**
 * Parser for the &lt;context:component-scan/&gt; element. Parsed metadata is
 * used to populate a {@link ComponentScanSpecification} object which is in turn
 * delegated to a {@link ComponentScanSpecificationExecutor} for actual scanning and
 * bean definition registration.
 * 
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see ComponentScanSpecificationExecutor
 * @see ComponentScan
 * @see ComponentScanAnnotationSpecificationCreator
 */
public class ComponentScanBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		XmlReaderContext readerContext = parserContext.getReaderContext();
		BeanDefinitionParserDelegate delegate = parserContext.getDelegate();
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		ComponentScanElementSpecificationCreator specCreator = new ComponentScanElementSpecificationCreator(parserContext);
		ComponentScanSpecification spec = specCreator.createFrom(element);

		ComponentScanSpecificationExecutor specExecutor = new ComponentScanSpecificationExecutor();
		specExecutor.setBeanDefinitionDefaults(delegate.getBeanDefinitionDefaults());
		specExecutor.setAutowireCandidatePatterns(delegate.getAutowireCandidatePatterns());

		ExecutorContext executorContext = new ExecutorContext();
		executorContext.setRegistry(registry);
		executorContext.setResourceLoader(readerContext.getResourceLoader());
		executorContext.setEnvironment(delegate.getEnvironment());

		specExecutor.execute(spec, executorContext);

		Object source = readerContext.extractSource(element);
		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);

		for (BeanDefinitionHolder beanDefHolder : specExecutor.getScannedBeans()) {
			compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefHolder));
		}

		// Register annotation config processors, if necessary.
		if ((spec.getIncludeAnnotationConfig() != null) && spec.getIncludeAnnotationConfig()) {
			Set<BeanDefinitionHolder> processorDefinitions =
					AnnotationConfigUtils.registerAnnotationConfigProcessors(registry, source);
			for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
				compositeDef.addNestedComponent(new BeanComponentDefinition(processorDefinition));
			}
		}

		readerContext.fireComponentRegistered(compositeDef);
		return null;
	}


}
