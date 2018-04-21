package com.jakewharton.sdksearch

import android.app.Activity
import android.app.Application
import com.jakewharton.sdksearch.ui.MainActivityModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.DispatchingAndroidInjector

@Component(modules = [
  MainActivityModule::class,
  ProductionDacModule::class,
  SearchPresenterModule::class
])
interface AppComponent {
  val activityInjector: DispatchingAndroidInjector<Activity>

  @Component.Builder
  interface Builder {
    @BindsInstance fun application(application: Application): Builder
    fun build(): AppComponent
  }
}

fun Application.createAppComponent() = DaggerAppComponent.builder()
    .application(this)
    .build()
