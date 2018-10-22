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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.inject.Qualifier;
import javax.inject.Scope;
import org.jetbrains.annotations.Nullable;

final class Reflection {
  static @Nullable Annotation findQualifier(Annotation[] annotations) {
    Annotation qualifier = null;
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().getAnnotation(Qualifier.class) != null) {
        if (qualifier != null) {
          throw new IllegalArgumentException(
              "Multiple qualifier annotations: " + qualifier + " and " + annotation);
        }
        qualifier = annotation;
      }
    }
    return qualifier;
  }

  static @Nullable Annotation findScope(Annotation[] annotations) {
    Annotation scope = null;
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().getAnnotation(Scope.class) != null) {
        if (scope != null) {
          throw new IllegalArgumentException(
              "Multiple scope annotations: " + scope + " and " + annotation);
        }
        scope = annotation;
      }
    }
    return scope;
  }

  static void trySet(Object instance, Field field, Object value) {
    try {
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to set " + value + " to " + field + " on " + instance, e);
    }
  }

  static Object tryInvoke(Object instance, Method method, Object... arguments) {
    try {
      return method.invoke(instance, arguments);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to invoke " + method + " on " + instance, e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      if (cause instanceof Error) throw (Error) cause;
      throw new RuntimeException("Unable to invoke " + method + " on " + instance, cause);
    }
  }

  private Reflection() {
    throw new AssertionError();
  }
}
