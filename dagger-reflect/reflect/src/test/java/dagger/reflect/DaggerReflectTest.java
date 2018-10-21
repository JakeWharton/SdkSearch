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

import dagger.Component;
import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public final class DaggerReflectTest {
  @Component abstract static class AbstractClass {
    @Component.Builder interface Builder {}
  }

  @Test public void abstractClassCreateFails() {
    try {
      DaggerReflect.create(AbstractClass.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("dagger.reflect.DaggerReflectTest.AbstractClass is not an interface. "
              + "Only interface components are supported.");
    }
  }

  @Test public void abstractClassBuilderFails() {
    try {
      DaggerReflect.builder(AbstractClass.class, AbstractClass.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("dagger.reflect.DaggerReflectTest.AbstractClass is not an interface. "
              + "Only interface components are supported.");
    }
  }

  interface NoAnnotation {
    @Component.Builder interface Builder {}
  }

  @Test public void noComponentAnnotationCreateFails() {
    try {
      DaggerReflect.create(NoAnnotation.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("dagger.reflect.DaggerReflectTest.NoAnnotation lacks @Component annotation");
    }
  }

  @Test public void noComponentAnnotationBuilderFails() {
    try {
      DaggerReflect.builder(NoAnnotation.class, NoAnnotation.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("dagger.reflect.DaggerReflectTest.NoAnnotation lacks @Component annotation");
    }
  }

  @Component
  interface AbstractBuilderClass {
    abstract class Builder {}
  }

  @Test public void abstractBuilderClassFails() {
    try {
      DaggerReflect.builder(AbstractBuilderClass.class, AbstractBuilderClass.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("dagger.reflect.DaggerReflectTest.AbstractBuilderClass.Builder is not an "
              + "interface. Only interface component builders are supported.");
    }
  }

  @Component
  interface NoBuilderAnnotation {
    interface Builder {}
  }

  @Ignore("@Component.Builder does not have runtime retention")
  @Test public void noComponentBuilderAnnotationFails() {
    try {
      DaggerReflect.builder(NoBuilderAnnotation.class, NoBuilderAnnotation.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("dagger.reflect.DaggerReflectTest.NoBuilderAnnotation.Builder lacks "
              + "@Component.Builder annotation");
    }
  }

  @Component(dependencies = { String.class, Runnable.class })
  interface ComponentWithDependencies { }

  @Test public void componentWithDependenciesCreateFails() {
    try {
      DaggerReflect.create(ComponentWithDependencies.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .isEqualTo("dagger.reflect.DaggerReflectTest.ComponentWithDependencies "
              + "declares dependencies [java.lang.String, java.lang.Runnable] "
              + "and therefore must be created with a builder");
    }
  }
}
