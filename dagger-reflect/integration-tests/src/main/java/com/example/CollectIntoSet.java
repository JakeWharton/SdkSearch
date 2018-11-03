package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.Set;
import javax.inject.Named;

@Component(modules = {
    CollectIntoSet.SetModule1.class,
    CollectIntoSet.SetModule2.class,
})
interface CollectIntoSet {
  Set<String> set();

  @Module
  abstract class SetModule1 {
    @Provides @IntoSet static String provideStatic() { return "providesStatic"; }
    @Provides @IntoSet static String provideInstance() { return "providesInstance"; }
  }

  @Module
  abstract class SetModule2 {
    @Provides @Named("named") static String provideNamed() { return "providesNamed"; }
    @Binds @IntoSet abstract String binds(@Named("named") String other);
  }
}
