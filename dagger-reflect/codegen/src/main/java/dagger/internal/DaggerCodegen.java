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
package dagger.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class DaggerCodegen {
  public static <C> C create(Class<C> componentClass) {
    return invokeStatic(findImplementationClass(componentClass), "create", componentClass);
  }

  public static <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
    return invokeStatic(findImplementationClass(componentClass), "builder", builderClass);
  }

  private static <C> Class<? extends C> findImplementationClass(Class<C> componentClass) {
    String implementationName =
        componentClass.getPackage().getName() + ".Dagger" + componentClass.getSimpleName();
    try {
      //noinspection unchecked Dagger compiler guarantees this cast to succeed.
      return (Class<? extends C>) componentClass.getClassLoader().loadClass(implementationName);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Unable to find generated component implementation "
          + implementationName
          + " for component "
          + componentClass.getName(), e);
    }
  }

  private static <T> T invokeStatic(Class<?> target, String name, Class<T> returnType) {
    Method method;
    try {
      method = target.getMethod(name);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Unable to find method '" + name + "' on " + target, e);
    }
    Object returnValue;
    try {
      returnValue = method.invoke(null);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to invoke method '" + name + "' on " + target, e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      if (cause instanceof Error) throw (Error) cause;
      throw new RuntimeException("Exception while reflectively invoking method", e);
    }
    return returnType.cast(returnValue);
  }

  private DaggerCodegen() {
    throw new AssertionError();
  }
}
