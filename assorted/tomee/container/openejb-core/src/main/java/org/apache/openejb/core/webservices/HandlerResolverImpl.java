/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.webservices;

import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;

import javax.naming.Context;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.openejb.InjectionProcessor.unwrap;

public class HandlerResolverImpl implements HandlerResolver {
    private final List<HandlerChainData> handlerChains;
    private final Collection<Injection> injections;
    private final Context context;
    private final List<InjectionProcessor<Handler>> handlerInstances = new ArrayList<InjectionProcessor<Handler>>();

    public HandlerResolverImpl(final List<HandlerChainData> handlerChains, final Collection<Injection> injections, final Context context) {
        this.handlerChains = handlerChains;
        this.injections = injections;
        this.context = context;
    }

    public void destroyHandlers() {
        final List<InjectionProcessor<Handler>> handlerInstances = new ArrayList<InjectionProcessor<Handler>>(this.handlerInstances);
        this.handlerInstances.clear();
        for (final InjectionProcessor<Handler> handlerInstance : handlerInstances) {
            handlerInstance.preDestroy();
        }
    }

    public List<Handler> getHandlerChain(final PortInfo portInfo) {
        List<Handler> chain = new ArrayList<Handler>();
        for (final HandlerChainData handlerChain : handlerChains) {
            List<Handler> handlers = buildHandlers(portInfo, handlerChain);
            handlers = sortHandlers(handlers);
            chain.addAll(handlers);
        }
        chain = sortHandlers(chain);
        return chain;
    }

    private List<Handler> buildHandlers(final PortInfo portInfo, final HandlerChainData handlerChain) {
        if (!matchServiceName(portInfo, handlerChain.getServiceNamePattern()) ||
                !matchPortName(portInfo, handlerChain.getPortNamePattern()) ||
                !matchBinding(portInfo, handlerChain.getProtocolBindings())) {
            return Collections.emptyList();
        }

        final List<Handler> handlers = new ArrayList<Handler>(handlerChain.getHandlers().size());
        for (final HandlerData handler : handlerChain.getHandlers()) {
            try {
                final Class<? extends Handler> handlerClass = handler.getHandlerClass().asSubclass(Handler.class);
                final InjectionProcessor<Handler> processor = new InjectionProcessor<Handler>(handlerClass,
                        injections,
                        handler.getPostConstruct(),
                        handler.getPreDestroy(),
                        unwrap(context));
                processor.createInstance();
                processor.postConstruct();
                final Handler handlerInstance = processor.getInstance();

                handlers.add(handlerInstance);
                handlerInstances.add(processor);
            } catch (final Exception e) {
                throw new WebServiceException("Failed to instantiate handler", e);
            }
        }
        return handlers;
    }

    private boolean matchServiceName(final PortInfo info, final QName namePattern) {
        return match(info == null ? null : info.getServiceName(), namePattern);
    }

    private boolean matchPortName(final PortInfo info, final QName namePattern) {
        return match(info == null ? null : info.getPortName(), namePattern);
    }

    private boolean matchBinding(final PortInfo info, final List bindings) {
        return match(info == null ? null : info.getBindingID(), bindings);
    }

    private boolean match(final String binding, final List bindings) {
        if (binding == null) {
            return bindings == null || bindings.isEmpty();
        } else {
            if (bindings == null || bindings.isEmpty()) {
                return true;
            } else {
                final String actualBindingURI = JaxWsUtils.getBindingURI(binding);
                final Iterator iter = bindings.iterator();
                while (iter.hasNext()) {
                    final String bindingToken = (String) iter.next();
                    final String bindingURI = JaxWsUtils.getBindingURI(bindingToken);
                    if (actualBindingURI.equals(bindingURI)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Performs basic localName matching
     */
    private boolean match(final QName name, final QName namePattern) {
        if (name == null) {
            return namePattern == null || namePattern.getLocalPart().equals("*");
        } else {
            if (namePattern == null) {
                return true;
            } else if (namePattern.getNamespaceURI() != null && !name.getNamespaceURI().equals(namePattern.getNamespaceURI())) {
                return false;
            } else {
                String localNamePattern = namePattern.getLocalPart();
                if (localNamePattern.equals("*")) {
                    // matches anything
                    return true;
                } else if (localNamePattern.endsWith("*")) {
                    // match start
                    localNamePattern = localNamePattern.substring(0, localNamePattern.length() - 1);
                    return name.getLocalPart().startsWith(localNamePattern);
                } else {
                    // match exact
                    return name.getLocalPart().equals(localNamePattern);
                }
            }
        }
    }

    /**
     * sorts the handlers into correct order. All of the logical handlers first
     * followed by the protocol handlers
     *
     * @param handlers
     * @return sorted list of handlers
     */
    private List<Handler> sortHandlers(final List<Handler> handlers) {
        final List<LogicalHandler> logicalHandlers = new ArrayList<LogicalHandler>();
        final List<Handler> protocolHandlers = new ArrayList<Handler>();

        for (final Handler handler : handlers) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler) handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        final List<Handler> sortedHandlers = new ArrayList<Handler>();
        sortedHandlers.addAll(logicalHandlers);
        sortedHandlers.addAll(protocolHandlers);
        return sortedHandlers;
    }
}
