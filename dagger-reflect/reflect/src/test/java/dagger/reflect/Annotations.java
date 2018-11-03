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

import com.google.auto.value.AutoAnnotation;
import javax.inject.Named;
import javax.inject.Singleton;
import org.junit.Test;

final class Annotations {
  @AutoAnnotation
  static Named named(String value) {
    return new AutoAnnotation_Annotations_named(value);
  }

  @AutoAnnotation
  static Singleton singleton() {
    return new AutoAnnotation_Annotations_singleton();
  }

  @AutoAnnotation
  static Test test() {
    return new AutoAnnotation_Annotations_test();
  }
}
