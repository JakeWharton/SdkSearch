package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.sdksearch.ui.MainActivityModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
  MainActivityModule::class,
  ProductionDacModule::class,
  SearchPresenterModule::class
])
interface ReleaseAppComponent : AppComponent {
  @Component.Builder
  interface Builder {
    @BindsInstance fun application(application: Application): Builder
    fun build(): ReleaseAppComponent
  }
}

fun Application.createAppComponent() = DaggerReleaseAppComponent.builder()
    .application(this)
    .build()
