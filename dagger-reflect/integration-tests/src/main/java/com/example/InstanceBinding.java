package com.example;

import dagger.BindsInstance;
import dagger.Component;

@Component
public interface InstanceBinding {
  String string();

  @Component.Builder
  interface Builder {
    @BindsInstance Builder string(String one);
    InstanceBinding build();
  }
}
