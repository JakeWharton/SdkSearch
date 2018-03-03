package com.jakewharton.sdksearch.api.circleci

import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import org.jetbrains.annotations.Nullable

@Component(modules = [
  CircleCiModule::class
])
interface CircleCiComponent {
  fun service(): CircleCiService

  @Component.Builder
  interface Builder {
    @BindsInstance fun token(token: String): Builder
    @BindsInstance fun client(@Nullable client: OkHttpClient): Builder
    fun build(): CircleCiComponent
  }

  companion object {
    fun builder(): Builder = DaggerCircleCiComponent.builder()
  }
}
