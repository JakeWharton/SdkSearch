package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public final class IntegrationTest {
  @Parameters(name = "{0}")
  public static Object[] parameters() {
    return Frontend.values();
  }

  @Parameter public Frontend frontend;

  @Test public void staticProvider() {
    StaticProvider component = frontend.create(StaticProvider.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test public void bindsProvider() {
    BindsProvider component = frontend.create(BindsProvider.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test public void bindsInstance() {
    InstanceBinding component =
        frontend.builder(InstanceBinding.class, InstanceBinding.Builder.class)
            .string("foo")
            .build();
    assertThat(component.string()).isEqualTo("foo");
  }
}
