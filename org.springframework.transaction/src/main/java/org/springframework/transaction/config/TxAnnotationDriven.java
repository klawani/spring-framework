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

import org.springframework.aop.config.ProxySpecification;
import org.springframework.context.InvalidSpecificationException;
import org.springframework.context.SourceAwareSpecification;
import org.springframework.context.Specification;
import org.springframework.context.annotation.ProxyType;
import org.springframework.transaction.PlatformTransactionManager;

public class TxAnnotationDriven implements Specification, ProxySpecification, SourceAwareSpecification {

	private PlatformTransactionManager txManager;
	private String txManagerName;

	private Integer order = null;

	private boolean proxyTargetClass = false;
	private ProxyType proxyType = ProxyType.SPRINGAOP;
	private Boolean exposeProxy = false;
	private Object source;
	private String sourceName;

	public TxAnnotationDriven(String txManagerName) {
		this.txManagerName = txManagerName;
	}

	public TxAnnotationDriven(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	public String transactionManagerName() {
		return this.txManagerName;
	}

	public PlatformTransactionManager transactionManager() {
		return this.txManager;
	}

	public TxAnnotationDriven proxyType(ProxyType proxyType) {
		this.proxyType = proxyType;
		return this;
	}

	public ProxyType proxyType() {
		return this.proxyType;
	}

	public TxAnnotationDriven proxyTargetClass(Boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
		return this;
	}

	public Boolean proxyTargetClass() {
		return this.proxyTargetClass;
	}

	public TxAnnotationDriven exposeProxy(Boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
		return this;
	}

	public Boolean exposeProxy() {
		return this.exposeProxy;
	}

	public TxAnnotationDriven order(Integer order) {
		this.order = order;
		return this;
	}

	public Integer order() {
		return this.order;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return this.source;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceName() {
		return this.sourceName;
	}

	public void validate() throws InvalidSpecificationException {
	}

}