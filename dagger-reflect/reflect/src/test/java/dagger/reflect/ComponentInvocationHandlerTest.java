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

import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static dagger.reflect.Annotations.named;
import static org.junit.Assert.fail;

public final class ComponentInvocationHandlerTest {
  interface Bindings {
    String one();
    Long two();
    Integer three();
  }

  @Test public void bindings() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .add(Key.of(null, Integer.class), new Binding.Instance<>(3))
        .build();
    Bindings component = ComponentInvocationHandler.create(Bindings.class, graph);
    assertThat(component.one()).isEqualTo("one");
    assertThat(component.two()).isEqualTo(2L);
    assertThat(component.three()).isEqualTo(3);
  }

  interface BindingQualifier {
    @Named("tres") Long three();
  }

  @Test public void bindingQualifier() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(named("tres"), Long.class), new Binding.Instance<>(3L))
        .build();
    BindingQualifier component = ComponentInvocationHandler.create(BindingQualifier.class, graph);
    assertThat(component.three()).isEqualTo(3L);
  }

  interface MembersInjectorVoid {
    void inject(Target instance);

    class Target {
      @Inject String one;
      @Inject Long two;
      @Inject Integer three;
    }
  }

  @Test public void membersInjectionReturnsVoid() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .add(Key.of(null, Integer.class), new Binding.Instance<>(3))
        .build();
    MembersInjectorVoid component =
        ComponentInvocationHandler.create(MembersInjectorVoid.class, graph);
    MembersInjectorVoid.Target instance = new MembersInjectorVoid.Target();
    component.inject(instance);
    assertThat(instance.one).isEqualTo("one");
    assertThat(instance.two).isEqualTo(2L);
    assertThat(instance.three).isEqualTo(3);
  }

  interface MembersInjectorInstance {
    Target inject(Target instance);

    class Target {
      @Inject String one;
      @Inject Long two;
      @Inject Integer three;
    }
  }

  @Test public void membersInjectionReturnsInstance() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .add(Key.of(null, Integer.class), new Binding.Instance<>(3))
        .build();
    MembersInjectorInstance component =
        ComponentInvocationHandler.create(MembersInjectorInstance.class, graph);
    MembersInjectorInstance.Target instance = new MembersInjectorInstance.Target();
    MembersInjectorInstance.Target returned = component.inject(instance);
    assertThat(returned).isSameAs(instance);
    assertThat(instance.one).isEqualTo("one");
    assertThat(instance.two).isEqualTo(2L);
    assertThat(instance.three).isEqualTo(3);
  }

  @SuppressWarnings("UnusedReturnValue") // Behavior under test.
  interface MembersInjectorWrongReturnType {
    String inject(Target instance);

    class Target {}
  }

  @Test public void membersInjectionWrongReturnType() {
    BindingGraph graph = new BindingGraph.Builder().build();
    MembersInjectorWrongReturnType component =
        ComponentInvocationHandler.create(MembersInjectorWrongReturnType.class, graph);
    MembersInjectorWrongReturnType.Target instance = new MembersInjectorWrongReturnType.Target();
    try {
      component.inject(instance);
      fail();
    } catch (IllegalStateException e) {
      // TODO assert message
    }
  }
}
