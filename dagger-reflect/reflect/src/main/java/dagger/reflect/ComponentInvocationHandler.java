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
import dagger.Subcomponent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Reflection.findQualifier;

final class ComponentInvocationHandler implements InvocationHandler {
  static <T> T create(Class<T> cls, BindingGraph graph) {
    return cls.cast(Proxy.newProxyInstance(cls.getClassLoader(), new Class[] { cls },
        new ComponentInvocationHandler(graph)));
  }

  private final BindingGraph graph;
  private final ConcurrentHashMap<Method, MethodInvocationHandler> handlers =
      new ConcurrentHashMap<>();

  private ComponentInvocationHandler(BindingGraph graph) {
    this.graph = graph;
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }

    MethodInvocationHandler handler = handlers.get(method);
    if (handler == null) {
      handler = createMethodInvocationHandler(method, args, graph);
      MethodInvocationHandler replaced = handlers.putIfAbsent(method, handler);
      if (replaced != null) {
        handler = replaced;
      }
    }
    return handler.invoke(args);
  }

  private static ComponentInvocationHandler.MethodInvocationHandler createMethodInvocationHandler(
      Method method, Object[] args, BindingGraph graph) {
    Type returnType = method.getGenericReturnType();
    Class<?>[] parameterTypes = method.getParameterTypes();

    if (args != null && args.length == 1) {
      boolean returnInstance;
      if (returnType == void.class) {
        returnInstance = false;
      } else if (method.getReturnType().equals(parameterTypes[0])) {
        returnInstance = true;
      } else {
        throw new IllegalStateException(); // TODO return type must be void or assignable
      }

      MembersInjector<Object> injector =
          (MembersInjector<Object>) ReflectiveMembersInjector.create(parameterTypes[0], graph);
      return new MembersInjectorMethodInvocationHandler(injector, returnInstance);
    }

    if (args == null || args.length == 0) {
      if (returnType instanceof Class<?>
          && ((Class<?>) returnType).getAnnotation(Subcomponent.class) != null) {
        throw notImplemented("Subcomponents");
      }

      Key key = Key.of(findQualifier(method.getDeclaredAnnotations()), returnType);
      Binding<?> binding = graph.getBinding(key);
      return new BindingMethodInvocationHandler(binding);
    }

    throw new IllegalStateException(method.toString()); // TODO unsupported method shape
  }

  private interface MethodInvocationHandler {
    Object invoke(Object[] args);
  }

  private static final class BindingMethodInvocationHandler implements MethodInvocationHandler {
    private final Binding<?> binding;

    BindingMethodInvocationHandler(Binding<?> binding) {
      this.binding = binding;
    }

    @Override public Object invoke(Object[] args) {
      return binding.get();
    }
  }

  private static final class MembersInjectorMethodInvocationHandler
      implements MethodInvocationHandler {
    private final MembersInjector<Object> membersInjector;
    private final boolean returnInstance;

    MembersInjectorMethodInvocationHandler(
        MembersInjector<Object> membersInjector,
        boolean returnInstance) {
      this.membersInjector = membersInjector;
      this.returnInstance = returnInstance;
    }

    @Override public Object invoke(Object[] args) {
      Object instance = args[0];
      membersInjector.injectMembers(instance);
      return returnInstance ? instance : null;
    }
  }
}
