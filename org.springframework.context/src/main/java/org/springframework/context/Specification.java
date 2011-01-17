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

package org.springframework.context;

/**
 * A value object specifying instructions for configuring a particular feature of the Spring
 * container e.g., component-scanning, JMX MBean exporting, AspectJ auto-proxying, etc.
 *
 * <p>Many features of the Spring container can be configured using either XML or annotations.
 * As one example, Spring's <em>component scanning</em> feature may be configured using
 * either the {@code <context:component-scan>} XML element or the {@code @ComponentScan}
 * annotation. These two options are equivalent to one another, and users choose between
 * them as a matter of convention or preference. Fundamentally, both are declarative mechanisms
 * for <em>specifying</em> how the Spring container should be configured.  A {@code Specification}
 * object, then, is a way of representing this configuration information independent of its
 * original source format, be it XML, annotations, or otherwise.
 *
 * <p>A {@link SpecificationCreator} may be used to read from the original XML or annotation
 * source and create a {@code Specification} object out of it. A {@link SpecificationExecutor}
 * is used to read and act upon the {@code Specification}; this is where the real work happens.
 * In the case of component scanning as above, it is within a {@code SpecificationExecutor} that
 * a bean definition scanner is created, configured and invoked against the base packages specified.
 *
 * <p>A {@code Specification} is responsible for {@linkplain #validate validating itself}.
 * For example, a component-scanning specification would check that at least one base package has
 * been specified, and otherwise throw an {@link InvalidSpecificationException}.
 *
 * <p>The primary purpose of the {@code Specification} abstraction and its {@code Creator}/{@code Executor}
 * pairs is to decouple XML and annotation parsing logic from container configuration logic. This separates
 * concerns and helps avoid duplication between XML and annotation parsers. These interfaces and their
 * implementations are not not intended for direct use by everyday application developers, but rather by
 * those creating new Spring XML namespaces or annotations i.e., framework developers.
 *
 * @author Chris Beams
 * @since 3.1
 * @see SpecificationCreator
 * @see SpecificationExecutor
 */
public interface Specification {

	/**
	 * Validate this specification instance to ensure all required properties
	 * have been set, including checks on mutually exclusive or mutually
	 * dependent properties.
	 * @throws InvalidSpecificationException if any errors are found
	 * @see AbstractSpecificationExecutor#execute(Specification)
	 */
	void validate() throws InvalidSpecificationException;

}
