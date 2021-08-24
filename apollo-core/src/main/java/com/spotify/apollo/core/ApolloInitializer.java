/*
 * -\-\-
 * Spotify Apollo Service Core (aka Leto)
 * --
 * Copyright (C) 2013 - 2021 Spotify AB
 * --
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
 * -/-/-
 */
package com.spotify.apollo.core;

import com.google.inject.Module;
import com.spotify.apollo.module.ApolloModule;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;

public class ApolloInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {


  private final String serviceName;
  private final Iterable<Module> guiceModules;

  public ApolloInitializer(String serviceName, Iterable<Module> guiceModules) {
    this.serviceName = serviceName;
    this.guiceModules = guiceModules;
  }

  @Override
  public void initialize(
      ConfigurableApplicationContext configurableApplicationContext) {
    configurableApplicationContext.getEnvironment().getPropertySources()
        .addFirst(new ApolloPropertySource(serviceName));
    configurableApplicationContext.getEnvironment().getPropertySources()
        .addFirst(new PropertySource<String>("serviceName") {
          @Override
          public Object getProperty(String name) {
            return "spring.application.name".equals(name) ? serviceName : null;
          }
        });

    for (Module allModule : guiceModules) {
      configurableApplicationContext.getBeanFactory().registerResolvableDependency(allModule.getClass(), allModule);
      String id = allModule instanceof ApolloModule ? ((ApolloModule) allModule).getId() : allModule.toString();
        configurableApplicationContext.getBeanFactory().registerSingleton(findUniqueId(id, configurableApplicationContext), allModule);
    }
  }

  private String findUniqueId(String id,
                              ConfigurableApplicationContext configurableApplicationContext) {
    if (!configurableApplicationContext.containsBean(id)) {
      return id;
    } else {
      for(int i = 1;; i++) {
        String newId  = id + "-" + i;
        if (!configurableApplicationContext.containsBean(newId)) {
          return newId;
        }
      }
    }
  }
}
