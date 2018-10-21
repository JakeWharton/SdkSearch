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

import javax.inject.Provider;

interface Binding<T> extends Provider<T> {
  boolean isLinked();
  Key[] dependencies();
  Binding<T> link(Binding<?>[] links);

  abstract class LinkedBinding<T> implements Binding<T> {
    @Override public final boolean isLinked() {
      return true;
    }

    @Override public final Key[] dependencies() {
      throw new UnsupportedOperationException();
    }

    @Override public final Binding<T> link(Binding<?>[] links) {
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
}
