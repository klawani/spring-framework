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

import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

/**
 * TODO SPR-7194: document
 *
 * @author Chris Beams
 * @since 3.1
 */
public abstract class AbstractSpecificationExecutor<S extends FeatureSpecification> implements SpecificationExecutor {

	/**
	 * {@inheritDoc}
	 * <p>This implementation {@linkplain FeatureSpecification#validate() validates} the
	 * given specification before delegating it to the {@link #doExecute(FeatureSpecification)}
	 * method.
	 * @throws InvalidSpecificationException if the given specification has has errors
	 */
	@SuppressWarnings("unchecked")
	public final void execute(FeatureSpecification spec, ExecutorContext executorContext) throws InvalidSpecificationException {
		Assert.notNull(spec, "Specification must not be null");
		Assert.notNull(spec, "ExecutorContext must not be null");
		Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.getClass(), AbstractSpecificationExecutor.class);
		Assert.isTrue(typeArg.equals(spec.getClass()), "Specification cannot be executed by this executor");
		spec.validate();
		doExecute((S)spec, executorContext);
	}

	/**
	 * Execute the given specification, usually resulting in registration of bean definitions
	 * against a bean factory.
	 * @param specification the {@linkplain FeatureSpecification#validate() validated} specification
	 */
	public abstract void doExecute(S specification, ExecutorContext executorContext);

}
