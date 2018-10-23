package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = StaticProvider.Module1.class)
interface StaticProvider {
  String string();

  @Module
  abstract class Module1 {
    @Provides static String string() {
      return "foo";
    }
  }
}
