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

import dagger.MembersInjector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.tryInvoke;
import static dagger.reflect.Reflection.trySet;

final class ReflectiveMembersInjector<T> implements MembersInjector<T> {
  static <T> MembersInjector<T> create(Class<T> cls, BindingGraph graph) {
    // TODO throw if interface?

    List<Bindings> hierarchyBindings = new LinkedList<>();
    Class<?> target = cls;
    while (target != Object.class) {
      Map<Field, Binding<?>> fieldBindings = new LinkedHashMap<>();
      Map<Method, Binding<?>[]> methodBindings = new LinkedHashMap<>();
      for (Field field : target.getDeclaredFields()) {
        if (field.getAnnotation(Inject.class) == null) {
          continue;
        }
        if ((field.getModifiers() & Modifier.PRIVATE) != 0) {
          throw new IllegalArgumentException(); // TODO report can't be private
        }
        if ((field.getModifiers() & Modifier.STATIC) != 0) {
          throw new IllegalArgumentException(); // TODO report can't be static
        }

        Key key = Key.of(findQualifier(field.getDeclaredAnnotations()), field.getGenericType());
        Binding<?> binding = graph.getBinding(key);

        fieldBindings.put(field, binding);
      }
      for (Method method : target.getDeclaredMethods()) {
        if (method.getAnnotation(Inject.class) == null) {
          continue;
        }
        if ((method.getModifiers() & Modifier.PRIVATE) != 0) {
          throw new IllegalArgumentException(); // TODO report can't be private
        }
        if ((method.getModifiers() & Modifier.STATIC) != 0) {
          throw new IllegalArgumentException(); // TODO report can't be static
        }

        Type[] parameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Binding<?>[] bindings = new Binding[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
          Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
          bindings[i] = graph.getBinding(key);
        }

        methodBindings.put(method, bindings);
      }
      hierarchyBindings.add(new Bindings(fieldBindings, methodBindings));

      target = target.getSuperclass();
    }
    // [@Inject] Fields and methods in superclasses are injected before those in subclasses.
    // we were walking up (getSuperclass), but spec says @Inject should be walking down the hierarchy
    Collections.reverse(hierarchyBindings);

    // TODO what if both are empty?
    return new ReflectiveMembersInjector<>(hierarchyBindings);
  }

  private final List<Bindings> classHierarchyBindings;

  private ReflectiveMembersInjector(List<Bindings> classHierarchyBindings) {
    this.classHierarchyBindings = classHierarchyBindings;
  }

  @Override public void injectMembers(T instance) {
    for (Bindings classBinding : classHierarchyBindings) {
      injectClassMembers(classBinding, instance);
    }
  }

  private void injectClassMembers(Bindings classBinding, T instance) {
    // [@Inject] Constructors are injected first, followed by fields, and then methods.
    // Note: the constructor injection is in dagger.reflect.Binding.UnlinkedJustInTime.dependencies
    for (Map.Entry<Field, Binding<?>> fieldBinding : classBinding.fieldBindings.entrySet()) {
      trySet(instance, fieldBinding.getKey(), fieldBinding.getValue().get());
    }
    for (Map.Entry<Method, Binding<?>[]> methodBinding : classBinding.methodBindings.entrySet()) {
      Binding<?>[] bindings = methodBinding.getValue();
      Object[] arguments = new Object[bindings.length];
      for (int i = 0; i < bindings.length; i++) {
        arguments[i] = bindings[i].get();
      }
      tryInvoke(instance, methodBinding.getKey(), arguments);
    }
  }

  private static class Bindings {
    final Map<Field, Binding<?>> fieldBindings;
    final Map<Method, Binding<?>[]> methodBindings;

    Bindings(
        Map<Field, Binding<?>> fieldBindings,
        Map<Method, Binding<?>[]> methodBindings) {
      this.fieldBindings = fieldBindings;
      this.methodBindings = methodBindings;
    }
  }
}
