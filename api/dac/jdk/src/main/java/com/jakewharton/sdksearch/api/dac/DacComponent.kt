package com.jakewharton.sdksearch.api.dac

import dagger.BindsInstance
import dagger.Component
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(modules = [
  DacModule::class
])
interface DacComponent {
  fun documentationService(): DocumentationService

  @Component.Builder
  interface Builder {
    @BindsInstance fun baseUrl(url: HttpUrl): Builder
    @BindsInstance fun client(client: OkHttpClient): Builder
    fun build(): DacComponent
  }

  companion object {
    fun builder(): Builder = DaggerDacComponent.builder()
  }
}
