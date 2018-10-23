package com.example;

import dagger.BindsInstance;
import dagger.Component;
import org.jetbrains.annotations.Nullable;

@Component
public interface InstanceBindingNull {
  @Nullable String string();

  @Component.Builder
  interface Builder {
    @BindsInstance Builder string(@Nullable String one);
    InstanceBindingNull build();
  }
}
