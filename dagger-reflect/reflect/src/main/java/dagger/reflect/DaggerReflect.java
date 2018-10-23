/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dagger.reflect;

import dagger.Component;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static dagger.reflect.Reflection.findScope;

public final class DaggerReflect {
  public static <C> C create(Class<C> componentClass) {
    if (!componentClass.isInterface()) {
      throw new IllegalArgumentException(componentClass.getCanonicalName() + " is not an interface. "
          + "Only interface components are supported.");
    }

    Component component = componentClass.getAnnotation(Component.class);
    if (component == null) {
      throw new IllegalArgumentException(
          componentClass.getCanonicalName() + " lacks @Component annotation");
    }

    Class<?>[] dependencies = component.dependencies();
    if (dependencies.length > 0) {
      StringBuilder builder = new StringBuilder(componentClass.getCanonicalName())
          .append(" declares dependencies [");
      for (int i = 0; i < dependencies.length; i++) {
        if (i > 0) builder.append(", ");
        builder.append(dependencies[i].getCanonicalName());
      }
      builder.append("] and therefore must be created with a builder");
      throw new IllegalArgumentException(builder.toString());
    }

    Annotation scope = findScope(componentClass.getAnnotations());
    if (scope != null) {
      throw notImplemented("Scoped components");
    }

    BindingGraph.Builder graphBuilder = new BindingGraph.Builder()
        .justInTimeProvider(new ReflectiveJustInTimeProvider());

    for (Class<?> module : component.modules()) {
      ReflectiveModuleParser.parse(module, null, graphBuilder);
    }
    return ComponentInvocationHandler.create(componentClass, graphBuilder.build());
  }

  public static <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
    if (!componentClass.isInterface()) {
      throw new IllegalArgumentException(componentClass.getCanonicalName()
          + " is not an interface. Only interface components are supported.");
    }
    if (!builderClass.isInterface()) {
      throw new IllegalArgumentException(builderClass.getCanonicalName()
          + " is not an interface. Only interface component builders are supported.");
    }

    Component component = componentClass.getAnnotation(Component.class);
    if (component == null) {
      throw new IllegalArgumentException(
          componentClass.getCanonicalName() + " lacks @Component annotation");
    }
    // TODO file a bug to get this retained for reflective access? Or maybe we don't care...
    //Component.Builder builder = builderClass.getAnnotation(Component.Builder.class);
    //if (builder == null) {
    //  throw new IllegalArgumentException(
    //      builderClass.getName() + " lacks @Component.Builder annotation");
    //}

    Set<Class<?>> modules = new LinkedHashSet<>();
    Collections.addAll(modules, component.modules());

    Set<Class<?>> dependencies = new LinkedHashSet<>();
    Collections.addAll(dependencies, component.dependencies());

    BindingGraph.Builder graphBuilder = new BindingGraph.Builder()
        .justInTimeProvider(new ReflectiveJustInTimeProvider());

    return ComponentBuilderInvocationHandler.create(componentClass, builderClass, graphBuilder,
        modules, dependencies);
  }

  static RuntimeException notImplemented(String feature) {
    return new UnsupportedOperationException(feature + " does not work yet, sorry!");
  }

  private DaggerReflect() {
    throw new AssertionError();
  }
}
