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

import static org.springframework.transaction.config.TxNamespaceHandler.getTransactionManagerName;

import org.springframework.context.annotation.ProxyType;
import org.springframework.context.xml.XmlElementSpecificationCreator;
import org.w3c.dom.Element;

public class TxAnnotationDrivenElementSpecificationCreator implements XmlElementSpecificationCreator {

	public TxAnnotationDriven createFrom(Element element) {
		return new TxAnnotationDriven(getTransactionManagerName(element))
			.proxyType(element.getAttribute("mode").equals("aspectj") ?
					ProxyType.ASPECTJ :
					ProxyType.SPRINGAOP)
			.order(element.hasAttribute("order") ?
					Integer.valueOf(element.getAttribute("order")) :
					null)
			.proxyTargetClass(
					Boolean.valueOf(element.getAttribute("proxy-target-class")))
			.exposeProxy(
					Boolean.valueOf(element.getAttribute("expose-proxy")));
	}

}
