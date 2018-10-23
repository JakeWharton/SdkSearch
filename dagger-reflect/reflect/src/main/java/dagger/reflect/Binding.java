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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.inject.Inject;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.tryInstantiate;
import static dagger.reflect.Reflection.tryInvoke;

interface Binding<T> extends Provider<T> {
  Key[] NO_DEPENDENCIES = new Key[0];

  boolean isLinked();
  Key[] dependencies();
  Binding<T> link(Binding<?>[] dependencies);

  abstract class LinkedBinding<T> implements Binding<T> {
    @Override public final boolean isLinked() {
      return true;
    }

    @Override public final Key[] dependencies() {
      throw new UnsupportedOperationException();
    }

    @Override public final Binding<T> link(Binding<?>[] dependencies) {
      throw new UnsupportedOperationException();
    }
  }

  abstract class UnlinkedBinding<T> implements Binding<T> {
    @Override public final boolean isLinked() {
      return false;
    }

    @Override public final T get() {
      throw new UnsupportedOperationException();
    }
  }

  final class Instance<T> extends LinkedBinding<T> {
    private final T value;

    Instance(T value) {
      this.value = value;
    }

    @Override public T get() {
      return value;
    }
  }

  final class UnlinkedBinds<T> extends UnlinkedBinding<T> {
    private final Method method;

    UnlinkedBinds(Method method) {
      this.method = method;
    }

    @Override public Key[] dependencies() {
      Type[] parameterTypes = method.getGenericParameterTypes();
      if (parameterTypes.length != 1) {
        throw new IllegalArgumentException(
            "@Binds methods must have a single parameter: " + method);
      }
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      Key dependency = Key.of(findQualifier(parameterAnnotations[0]), parameterTypes[0]);
      return new Key[] { dependency };
    }

    @Override public Binding<T> link(Binding<?>[] dependencies) {
      return (Binding<T>) dependencies[0];
    }
  }

  final class UnlinkedProvides<T> extends UnlinkedBinding<T> {
    private final @Nullable Object instance;
    private final Method method;

    UnlinkedProvides(@Nullable Object instance, Method method) {
      this.instance = instance;
      this.method = method;
    }

    @Override public Key[] dependencies() {
      Type[] parameterTypes = method.getGenericParameterTypes();
      if (parameterTypes.length == 0) {
        return NO_DEPENDENCIES;
      }
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      Key[] dependencies = new Key[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        dependencies[i] = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
      }
      return dependencies;
    }

    @Override public Binding<T> link(Binding<?>[] dependencies) {
      return new LinkedProvides<>(instance, method, dependencies);
    }
  }

  final class LinkedProvides<T> extends LinkedBinding<T> {
    private final @Nullable Object instance;
    private final Method method;
    private final Binding<?>[] dependencies;

    LinkedProvides(@Nullable Object instance, Method method, Binding<?>[] dependencies) {
      this.instance = instance;
      this.method = method;
      this.dependencies = dependencies;
    }

    @Override public T get() {
      Object[] arguments = new Object[dependencies.length];
      for (int i = 0; i < arguments.length; i++) {
        arguments[i] = dependencies[i].get();
      }
      return (T) tryInvoke(instance, method, arguments);
    }
  }

  final class UnlinkedJustInTime<T> extends UnlinkedBinding<T> {
    private final Class<T> cls;
    private final Constructor<T> constructor;

    UnlinkedJustInTime(Class<T> cls, Constructor<T> constructor) {
      this.cls = cls;
      this.constructor = constructor;
    }

    @Override public Key[] dependencies() {
      // TODO field and method bindings? reuse some/all of reflective members injector somehow?
      Class<?> target = cls;
      while (target != Object.class) {
        for (Field field : target.getDeclaredFields()) {
          if (field.getAnnotation(Inject.class) != null) {
            throw notImplemented("@Inject on fields in just-in-time bindings");
          }
        }
        for (Method method : target.getDeclaredMethods()) {
          if (method.getAnnotation(Inject.class) != null) {
            throw notImplemented("@Inject on methods in just-in-time bindings");
          }
        }
        target = target.getSuperclass();
      }

      Type[] parameterTypes = constructor.getGenericParameterTypes();
      Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
      Key[] dependencies = new Key[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        dependencies[i] = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
      }
      return dependencies;
    }

    @Override public Binding<T> link(Binding<?>[] dependencies) {
      return new LinkedJustInTime<>(constructor, dependencies);
    }
  }

  final class LinkedJustInTime<T> extends LinkedBinding<T> {
    private final Constructor<T> constructor;
    private final Binding<?>[] dependencies;

    LinkedJustInTime(Constructor<T> constructor, Binding<?>[] dependencies) {
      this.constructor = constructor;
      this.dependencies = dependencies;
    }

    @Override public T get() {
      Object[] arguments = new Object[dependencies.length];
      for (int i = 0; i < dependencies.length; i++) {
        arguments[i] = dependencies[i].get();
      }
      return tryInstantiate(constructor, arguments);
    }
  }
}
