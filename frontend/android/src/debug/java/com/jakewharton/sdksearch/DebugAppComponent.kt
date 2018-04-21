package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.sdksearch.debug.updater.UpdateActivityModule
import com.jakewharton.sdksearch.ui.MainActivityModule
import dagger.BindsInstance
import dagger.Component

@Component(modules = [
  UpdateActivityModule::class,
  UpdateConfigModule::class,
  MainActivityModule::class,
  ProductionDacModule::class,
  SearchPresenterModule::class
])
interface DebugAppComponent : AppComponent {
  @Component.Builder
  interface Builder {
    @BindsInstance fun application(application: Application): Builder
    fun build(): DebugAppComponent
  }
}

fun Application.createAppComponent() = DaggerDebugAppComponent.builder()
    .application(this)
    .build()
