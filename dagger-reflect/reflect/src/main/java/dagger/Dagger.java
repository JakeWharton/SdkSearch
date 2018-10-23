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
package dagger;

import dagger.reflect.DaggerReflect;

public final class Dagger {
  public static <C> C create(Class<C> componentClass) {
    return DaggerReflect.create(componentClass);
  }

  public static <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
    return DaggerReflect.builder(componentClass, builderClass);
  }

  private Dagger() {
    throw new AssertionError();
  }
}
