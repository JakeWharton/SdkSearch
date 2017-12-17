package com.jakewharton.sdksearch.api.dac

import dagger.BindsInstance
import dagger.Component
import okhttp3.HttpUrl
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
    fun build(): DacComponent
  }

  companion object {
    fun builder(): Builder = DaggerDacComponent.builder()
  }
}
