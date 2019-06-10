package com.jakewharton.sdksearch.api.dac

import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient

@Component(modules = [
  DacModule::class
])
interface DacComponent {
  fun documentationService(): DocumentationService

  @Component.Factory
  interface Factory {
    fun create(@BindsInstance client: OkHttpClient): DacComponent
  }

  companion object : Factory by DaggerDacComponent.factory()
}
