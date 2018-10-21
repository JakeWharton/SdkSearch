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

import com.google.auto.value.AutoValue;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.jetbrains.annotations.Nullable;

@AutoValue
abstract class Key {
  static Key of(@Nullable Annotation qualifier, Type type) {
    return new AutoValue_Key(qualifier, type);
  }

  abstract @Nullable Annotation qualifier();
  abstract Type type();

  @Override public final String toString() {
    Annotation qualifier = qualifier();
    String type = type().getTypeName(); // TODO 1.8 method
    return qualifier != null
        ? qualifier.toString() + ' ' + type
        : type;
  }
}
