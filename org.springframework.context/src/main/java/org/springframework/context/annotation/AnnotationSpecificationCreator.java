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

import org.springframework.context.FeatureSpecification;
import org.springframework.context.SpecificationCreator;
import org.springframework.context.SpecificationExecutor;
import org.springframework.context.xml.XmlElementSpecificationCreator;
import org.springframework.core.type.AnnotationMetadata;

/**
 * TODO SPR-7194: documentation (clean up)
 *
 * Interface for parsing {@link AnnotationMetadata} into a more generic
 * {@link FeatureSpecification} object. Used in conjunction with a
 * {@link SpecificationExecutor} to provide a source-agnostic approach to
 * handling configuration metadata.
 *
 * <p>For example, Spring's component-scanning can be configured via XML using
 * the {@code context:component-scan} element or via the {@link ComponentScan}
 * annotation. In either case, the metadata is the same -- only the source
 * format differs.  {@link ComponentScanElementSpecificationCreator} is used to create
 * a specification from the {@code <context:component-scan>} XML element, while
 * {@link ComponentScanAnnotationSpecificationCreator} creates a specification from the
 * the annotation style. They both produce a {@link ComponentScanSpecification}
 * object that is ultimately delegated to a {@link ComponentScanSpecificationExecutor}
 * which understands how to configure a {@link ClassPathBeanDefinitionScanner},
 * perform actual scanning, and register actual bean definitions against the
 * container.
 *
 * @author Chris Beams
 * @since 3.1
 * @see FeatureSpecification
 * @see SpecificationExecutor
 * @see XmlElementSpecificationCreator
 */
public interface AnnotationSpecificationCreator extends SpecificationCreator<AnnotationMetadata> {

	/**
	 * Whether this writer is capable of handling the metadata in question
	 * i.e., whether the metadata contains the annotation(s) this writer knows
	 * about.
	 */
	boolean accepts(AnnotationMetadata metadata);

	/**
	 * Parse the given annotation metadata into a more general
	 * {@link FeatureSpecification} object.
	 * @param metadata the annotation metadata to parse
	 * @return the metadata definition, suitable for reading by
	 * a {@link SpecificationExecutor}.
	 * @see AnnotationSpecificationCreator
	 */
	FeatureSpecification createFrom(AnnotationMetadata metadata);

}
