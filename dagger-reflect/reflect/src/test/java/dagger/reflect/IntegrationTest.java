package dagger.reflect;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class IntegrationTest {
  @Component(modules = StaticProvider.Module1.class)
  interface StaticProvider {
    String foo();

    @Module
    abstract class Module1 {
      @Provides static String foo() {
        return "foo";
      }
    }
  }

  @Test public void staticProvider() {
    StaticProvider component = DaggerReflect.create(StaticProvider.class);
    assertThat(component.foo()).isEqualTo("foo");
  }

  @Component(modules = BindsProvider.Module1.class)
  interface BindsProvider {
    CharSequence foo();

    @Module
    abstract class Module1 {
      @Provides static String foo() {
        return "foo";
      }
      @Binds abstract CharSequence fooBinds(String foo);
    }
  }

  @Test public void bindsProvider() {
    BindsProvider component = DaggerReflect.create(BindsProvider.class);
    assertThat(component.foo()).isEqualTo("foo");
  }
}
