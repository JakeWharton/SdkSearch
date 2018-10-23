package com.example;

import dagger.Component;
import javax.inject.Inject;

@Component
interface JustInTimeConstructor {
  Thing thing();

  final class Thing {
    @Inject Thing() {}
  }
}
