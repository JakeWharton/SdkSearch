package com.jakewharton.sdksearch.api.dac

import dagger.BindsInstance
import dagger.Component
import okhttp3.HttpUrl
import javax.inject.Singleton

@Singleton
@Component(modules = [
  ApiModule::class
])
interface ApiComponent {
  fun documentationService(): DocumentationService

  @Component.Builder
  interface Builder {
    @BindsInstance fun baseUrl(url: HttpUrl): Builder
    fun build(): ApiComponent
  }

  companion object {
    fun builder(): Builder = DaggerApiComponent.builder()
  }
}
