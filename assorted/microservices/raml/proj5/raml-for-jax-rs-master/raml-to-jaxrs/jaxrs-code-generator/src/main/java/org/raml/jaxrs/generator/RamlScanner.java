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
package org.raml.jaxrs.generator;


import org.raml.jaxrs.generator.v08.V08Finder;
import org.raml.jaxrs.generator.v08.V08TypeRegistry;
import org.raml.jaxrs.generator.v10.ResourceHandler;
import org.raml.jaxrs.generator.v10.V10Finder;
import org.raml.jaxrs.generator.v10.V10TypeRegistry;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v10.resources.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * Created by Jean-Philippe Belanger on 10/26/16. Just potential zeroes and ones
 */
public class RamlScanner {

  private final Configuration configuration;

  public RamlScanner(Configuration configuration) {
    this.configuration = configuration;
  }


  public void handle(String resourceName) throws IOException, GenerationException {

    handle(RamlScanner.class.getResource(resourceName));
  }

  public void handle(File resource) throws IOException, GenerationException {

    handle(new FileInputStream(resource), resource.getAbsoluteFile().getParentFile().getAbsolutePath() + "/");
  }

  public void handle(URL resourceName) throws IOException, GenerationException {

    handle(resourceName.openStream(), ".");
  }

  public void handle(InputStream stream, String directory) throws GenerationException, IOException {

    RamlModelResult result =
        new RamlModelBuilder().buildApi(new InputStreamReader(stream), directory);
    if (result.hasErrors()) {
      throw new GenerationException(result.getValidationResults());
    }

    if (result.isVersion08()) {
      handle(result.getApiV08());
    } else {
      handle(result.getApiV10());
    }
  }

  public void handle(org.raml.v2.api.model.v10.api.Api api) throws IOException {

    V10TypeRegistry registry = new V10TypeRegistry();
    CurrentBuild build = new CurrentBuild(new V10Finder(api, registry), api);
    configuration.setupBuild(build);
    build.constructClasses();

    ResourceHandler resourceHandler = new ResourceHandler(build);


    // handle resources.
    for (Resource resource : api.resources()) {
      resourceHandler.handle(registry, resource);
    }


    build.generate(configuration.getOutputDirectory());
  }


  public void handle(org.raml.v2.api.model.v08.api.Api api) throws IOException {

    GAbstractionFactory factory = new GAbstractionFactory();
    V08TypeRegistry registry = new V08TypeRegistry();
    V08Finder typeFinder = new V08Finder(api, factory, registry);
    CurrentBuild build = new CurrentBuild(typeFinder, null);
    configuration.setupBuild(build);

    build.constructClasses();

    ResourceHandler resourceHandler = new ResourceHandler(build);


    // handle resources.
    for (org.raml.v2.api.model.v08.resources.Resource resource : api.resources()) {
      resourceHandler.handle(typeFinder.globalSchemas(), registry, resource);
    }

    build.generate(configuration.getOutputDirectory());
  }

}
