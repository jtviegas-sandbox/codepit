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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.httpd;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

@EnableServices({ "httpejbd" })
@RunWith(ApplicationComposer.class)
public class CustomHttpMethodTest {
    private int nextAvailablePort = -1;

    @Test
    public void custom() throws URISyntaxException, IOException {
        final AtomicReference<String> method = new AtomicReference<String>();
        final HttpListenerRegistry registry = SystemInstance.get().getComponent(HttpListenerRegistry.class);
        registry.addHttpListener(new HttpListener() {
            @Override
            public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
                method.set(request.getMethod());
            }
        }, "/custom");
        try {
            final URL url = new URL("http://localhost:" + nextAvailablePort + "/custom");
            final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());
            connection.setRequestMethod("OPTIONS");
            final InputStream inputStream = connection.getInputStream();
            IO.slurp(inputStream);
            assertEquals("OPTIONS", method.get());
            inputStream.close();
        } finally {
            registry.removeHttpListener("/custom");
        }
    }

    @Configuration
    public Properties props() {
        nextAvailablePort = NetworkUtil.getNextAvailablePort();
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(nextAvailablePort)).build();
    }

    @Module
    public EjbJar jar() {
        return new EjbJar();
    }
}
