package com.example;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = BuilderImplicitModules.Module1.class)
public interface BuilderImplicitModules {
  String string();

  @Module
  abstract class Module1 {
    @Provides static String string(Long value) {
      return Long.toString(value);
    }
  }

  @Component.Builder
  interface Builder {
    @BindsInstance Builder value(Long value);
    BuilderImplicitModules build();
  }
}
