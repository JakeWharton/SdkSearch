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

import dagger.reflect.Binding.UnlinkedIntoSet;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

final class BindingGraph {
  private final ConcurrentHashMap<Key, Binding<?>> bindings;
  private final JustInTimeProvider jitProvider;

  private BindingGraph(Map<Key, Binding<?>> bindings, JustInTimeProvider jitProvider) {
    this.bindings = new ConcurrentHashMap<>(bindings);
    this.jitProvider = jitProvider;
  }

  Binding<?> getBinding(Key key) {
    Binding<?> binding = locateBinding(key);
    return binding.isLinked()
        ? binding
        : performLinking(key, binding, new LinkedHashMap<>());
  }

  private Binding<?> performLinking(Key key, Binding<?> unlinked, Map<Key, Binding<?>> chain) {
    chain.put(key, unlinked);

    Key[] dependencyKeys = unlinked.dependencies();
    Binding<?>[] dependencies = new Binding[dependencyKeys.length];

    for (int i = 0; i < dependencyKeys.length; i++) {
      Key dependencyKey = dependencyKeys[i];
      Binding<?> dependency = locateBinding(dependencyKey);

      if (!dependency.isLinked()) {
        if (chain.containsKey(dependencyKey)) {
          StringBuilder builder = new StringBuilder("Dependency cycle detected!\n");
          for (Map.Entry<Key, Binding<?>> entry : chain.entrySet()) {
            builder.append(" * Requested: ")
                .append(entry.getKey())
                .append("\n     from ")
                .append(entry.getValue())
                .append('\n');
          }
          builder.append(" * Requested: ")
              .append(dependencyKey)
              .append("\n     which forms a cycle.");
          throw new IllegalStateException(builder.toString());
        }

        dependency = performLinking(dependencyKey, dependency, chain);
      }

      dependencies[i] = dependency;
    }

    chain.remove(key);

    Binding<?> linked = unlinked.link(dependencies);
    if (!linked.isLinked()) {
      throw new IllegalStateException("A call to link on "
          + unlinked
          + " returned "
          + linked
          + " but it reported itself as unlinked.");
    }

    return bindings.replace(key, unlinked, linked)
        ? linked
        : bindings.get(key); // We raced another thread and lost.
  }

  private Binding<?> locateBinding(Key key) {
    Binding<?> binding = bindings.get(key);
    if (binding != null) {
      return binding;
    }

    Binding<?> jitBinding = jitProvider.create(key);
    if (jitBinding != null) {
      Binding<?> replaced = bindings.putIfAbsent(key, jitBinding);
      return replaced != null
          ? replaced // We raced another thread and lost.
          : jitBinding;
    }

    throw new IllegalArgumentException("Unable to locate binding for " + key);
  }

  static final class Builder {
    private final Map<Key, Binding<?>> bindings = new LinkedHashMap<>();
    private JustInTimeProvider jitProvider = JustInTimeProvider.NONE;

    Builder justInTimeProvider(JustInTimeProvider jitProvider) {
      if (jitProvider == null) throw new NullPointerException("jitProvider == null");
      this.jitProvider = jitProvider;
      return this;
    }

    Builder add(Key key, Binding<?> binding) {
      if (key == null) throw new NullPointerException("key == null");
      if (binding == null) throw new NullPointerException("binding == null");

      Binding<?> original = bindings.get(key);
      if (original != null) {
        Binding<?> merged = merge(original, binding);
        if (merged != null) {
          bindings.remove(key);
          binding = merged;
        }
      }

      Binding<?> replaced = bindings.put(key, binding);
      if (replaced != null) {
        throw new IllegalStateException(
            "Duplicate binding for " + key + ": " + replaced + " and " + binding);
      }
      return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Binding<?> merge(Binding<?> oldBinding, Binding<?> newBinding) {
      if (oldBinding instanceof Binding.UnlinkedIntoSet && newBinding instanceof Binding.UnlinkedIntoSet) {
        Binding[] oldBindings = ((UnlinkedIntoSet) oldBinding).getBindings();
        Binding[] newBindings = ((UnlinkedIntoSet) newBinding).getBindings();
        Binding[] allBindings = Arrays.copyOf(oldBindings, oldBindings.length + newBindings.length);
        System.arraycopy(newBindings, 0, allBindings, oldBindings.length, newBindings.length);
        return new Binding.UnlinkedIntoSet(allBindings);
      }
      return null;
    }

    BindingGraph build() {
      return new BindingGraph(bindings, jitProvider);
    }
  }

  interface JustInTimeProvider {
    @Nullable Binding<?> create(Key key);

    JustInTimeProvider NONE = key -> null;
  }
}
