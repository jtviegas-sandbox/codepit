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
package org.raml.jaxrs.generator.utils;

import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.GAbstractionFactory;
import org.raml.jaxrs.generator.builders.CodeContainer;
import org.raml.jaxrs.generator.builders.resources.ResourceBuilder;
import org.raml.jaxrs.generator.v10.V10Finder;
import org.raml.jaxrs.generator.v10.V10GResource;
import org.raml.jaxrs.generator.v10.V10TypeRegistry;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.resources.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Jean-Philippe Belanger on 12/25/16. Just potential zeroes and ones
 */
public class RamlV10 {

  public static Resource withV10(Object test, String raml) {

    return buildApiV10(test, raml).resources().get(0);
  }

  public static Api buildApiV10(Object test, String raml) {

    RamlModelResult ramlModelResult =
        new RamlModelBuilder().buildApi(
                                        new InputStreamReader(test.getClass().getResourceAsStream(raml)), ".");
    if (ramlModelResult.hasErrors()) {
      for (ValidationResult validationResult : ramlModelResult.getValidationResults()) {
        System.out.println(validationResult.getMessage());
      }
      throw new AssertionError();
    } else {
      return ramlModelResult.getApiV10();
    }
  }

  public static void buildResourceV10(Object test, String raml, CodeContainer<TypeSpec> container,
                                      String name, String uri) throws IOException {

    Api api = buildApiV10(test, raml);
    V10TypeRegistry registry = new V10TypeRegistry();
    CurrentBuild currentBuild = new CurrentBuild(new V10Finder(api, registry), api);
    currentBuild.constructClasses();
    ResourceBuilder builder =
        new ResourceBuilder(currentBuild, new V10GResource(registry, new GAbstractionFactory(), api
            .resources().get(0)), name, uri);
    builder.output(container);
  }


  public static CurrentBuild buildType(Object test, String raml, V10TypeRegistry registry,
                                       String name, String uri) throws IOException, URISyntaxException {

    URL u = test.getClass().getResource(raml);
    File ramlFile = new File(u.toURI());

    RamlModelResult ramlModelResult =
        new RamlModelBuilder().buildApi(
                                        new InputStreamReader(test.getClass().getResourceAsStream(raml)),
                                        ramlFile.getAbsolutePath());
    if (ramlModelResult.hasErrors()) {
      for (ValidationResult validationResult : ramlModelResult.getValidationResults()) {
        System.out.println(validationResult.getMessage());
      }
      throw new AssertionError();
    } else {
      CurrentBuild currentBuild =
          new CurrentBuild(new V10Finder(ramlModelResult.getApiV10(), registry),
                           ramlModelResult.getApiV10());
      currentBuild.constructClasses();
      return currentBuild;
    }
  }
}
