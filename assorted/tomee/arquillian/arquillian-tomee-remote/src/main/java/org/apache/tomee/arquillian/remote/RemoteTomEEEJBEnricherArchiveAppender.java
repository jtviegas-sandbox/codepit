/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomee.arquillian.remote;

import org.apache.openejb.arquillian.common.TomEEInjectionEnricher;
import org.apache.openejb.arquillian.common.enrichment.OpenEJBEnricher;
import org.apache.openejb.arquillian.common.mockito.MockitoEnricher;
import org.apache.openejb.arquillian.transaction.OpenEJBTransactionProvider;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class RemoteTomEEEJBEnricherArchiveAppender implements AuxiliaryArchiveAppender {
    @Override
    public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "arquillian-tomee-archive-appender.jar")
                .addClasses(RemoteTomEEObserver.class, RemoteTomEERemoteExtension.class, OpenEJBTransactionProvider.class)
                .addClasses(OpenEJBEnricher.class, TomEEInjectionEnricher.class, MockitoEnricher.class)
                .addAsServiceProvider(RemoteLoadableExtension.class, RemoteTomEERemoteExtension.class);
    }
}