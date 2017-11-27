/*
 * Copyright 2013-2017 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.jaxrs.converter.model;

import com.google.common.base.Optional;
import org.raml.api.Annotable;
import org.raml.api.RamlEntity;
import org.raml.api.RamlResourceMethod;
import org.raml.api.RamlSupportedAnnotation;

import java.lang.annotation.Annotation;

/**
 * Created by Jean-Philippe Belanger on 3/29/17. Just potential zeroes and ones
 */
public class JaxRsRamlSupportedAnnotation implements RamlSupportedAnnotation {

  private final Class<? extends Annotation> annotation;

  public JaxRsRamlSupportedAnnotation(Class<? extends Annotation> annotation) {
    this.annotation = annotation;
  }

  @Override
  public Class<? extends Annotation> getAnnotation() {
    return annotation;
  }

  @Override
  public <T extends Annotation> Optional<T> getAnnotationInstance(Annotable annotable) {

    return (Optional<T>) annotable.getAnnotation(annotation);
  }
}
