package com.jakewharton.sdksearch.api.circleci

import dagger.BindsInstance
import dagger.Component

@Component(modules = [
  CircleCiModule::class
])
interface CircleCiComponent {
  fun service(): CircleCiService

  @Component.Builder
  interface Builder {
    @BindsInstance
    fun token(token: String): Builder
    fun build(): CircleCiComponent
  }

  companion object {
    fun builder(): Builder = DaggerCircleCiComponent.builder()
  }
}
