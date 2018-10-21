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

import dagger.BindsInstance;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.emptySet;
import static org.junit.Assert.fail;

public final class ComponentBuilderInvocationHandlerTest {
  public interface InstanceBinding {
    String one();

    interface Builder {
      @BindsInstance Builder one(String one);
      InstanceBinding build();
    }
  }

  @Test public void instance() {
    InstanceBinding.Builder builder =
        ComponentBuilderInvocationHandler.create(InstanceBinding.class,
            InstanceBinding.Builder.class, emptySet());
    InstanceBinding component = builder.one("one").build();
    assertThat(component.one()).isEqualTo("one");
  }

  interface PackagePrivateComponent {
    interface Builder {}
  }

  @Test public void packagePrivateComponentFails() {
    try {
      ComponentBuilderInvocationHandler.create(PackagePrivateComponent.class,
          PackagePrivateComponent.Builder.class, emptySet());
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("Component interface "
              + "dagger.reflect.ComponentBuilderInvocationHandlerTest$PackagePrivateComponent "
              + "must be public in order to be reflectively created");
    }
  }
}
