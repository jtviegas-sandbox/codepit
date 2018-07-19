package org.aprestos.labs.api.bootexpone.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan(basePackages = { "org.aprestos.labs.api.bootexpone.resources" })
public class SwaggerConfig {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2).securityContexts(securityContext()).useDefaultResponseMessages(false)
        .select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
  }

  private List<SecurityContext> securityContext() {
    List<SecurityContext> r = new ArrayList<>();
    r.add(SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.any()).build());
    return r;
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return newArrayList(new SecurityReference("apikey", authorizationScopes));
  }

}
