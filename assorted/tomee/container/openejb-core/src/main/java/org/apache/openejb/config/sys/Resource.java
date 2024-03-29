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
package org.apache.openejb.config.sys;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jar" type="{http://www.openejb.org/System/Configuration}JarFileLocation" />
 *       &lt;attribute name="jndi" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="provider" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Resource")
public class Resource extends AbstractService {
    @XmlAttribute
    protected String jndi;

    @XmlElement(name = "aliases")
    protected List<String> aliases = new ArrayList<String>();

    public Resource(final String id) {
        super(id);
    }

    public Resource(final String id, final String type) {
        super(id, type);
    }

    public Resource(final String id, final String type, final String provider) {
        super(id, type, provider);
    }

    public Resource() {
    }

    /**
     * Gets the value of the jndi property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJndi() {
        return jndi;
    }

    /**
     * Sets the value of the jndi property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJndi(final String value) {
        this.jndi = value;
    }

    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Resource resource = (Resource) o;

        if (jndi != null ? !jndi.equals(resource.jndi) : resource.jndi != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (jndi != null ? jndi.hashCode() : 0);
        return result;
    }
}
