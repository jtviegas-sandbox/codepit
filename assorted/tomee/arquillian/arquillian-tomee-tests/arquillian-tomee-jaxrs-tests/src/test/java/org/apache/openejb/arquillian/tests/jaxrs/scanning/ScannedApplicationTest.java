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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.scanning;

import junit.framework.Assert;
import org.apache.openejb.arquillian.tests.jaxrs.JaxrsTest;
import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class ScannedApplicationTest extends JaxrsTest {

    @Deployment(testable = false)
    public static WebArchive archive() {
        return new WebModule(ScannedApplicationTest.class, ScannedApplicationTest.class).getArchive();
    }

    @Test
    public void invoke() throws Exception {
        // http://localhost:11080/ScannedApplicationTest/echo/.
        Assert.assertEquals("olleh", get("rest/echo/reverse/hello"));

    }

}
