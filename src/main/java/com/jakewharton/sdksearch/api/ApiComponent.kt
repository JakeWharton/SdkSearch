package com.jakewharton.sdksearch.api

import dagger.BindsInstance
import dagger.Component
import okhttp3.HttpUrl

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
