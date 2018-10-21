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

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static dagger.reflect.Annotations.named;
import static org.junit.Assert.fail;

public final class BindingGraphTest {
  class DummyBinding extends Binding.UnlinkedBinding<Object> {
    private final Object value;
    private final Key[] dependencies;

    DummyBinding(Object value, Key... dependencies) {
      this.value = value;
      this.dependencies = dependencies;
    }

    @Override public Key[] dependencies() {
      return dependencies;
    }

    @Override public Binding<Object> link(Binding<?>[] links) {
      return new Instance<>(value);
    }

    @Override public String toString() {
      return "DummyBinding{" + value + '}';
    }
  }

  @Test public void cycleDetection() {
    Key keyA = Key.of(named("A"), String.class);
    Key keyB = Key.of(named("B"), String.class);
    Key keyC = Key.of(named("C"), String.class);
    BindingGraph graph = new BindingGraph.Builder()
        .add(keyA, new DummyBinding("A", keyB))
        .add(keyB, new DummyBinding("B", keyC))
        .add(keyC, new DummyBinding("C", keyA))
        .build();
    try {
      graph.getBinding(keyA);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("Dependency cycle detected!\n"
          + " * Requested: @javax.inject.Named(\"A\") java.lang.String\n"
          + "     from DummyBinding{A}\n"
          + " * Requested: @javax.inject.Named(\"B\") java.lang.String\n"
          + "     from DummyBinding{B}\n"
          + " * Requested: @javax.inject.Named(\"C\") java.lang.String\n"
          + "     from DummyBinding{C}\n"
          + " * Requested: @javax.inject.Named(\"A\") java.lang.String\n"
          + "     which forms a cycle.");
    }
  }
}
