package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.sdksearch.ui.MainActivityModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
  MainActivityModule::class,
  ItemStoreModule::class,
  SearchPresenterModule::class,
  AndroidInjectionModule::class
])
interface ReleaseAppComponent : AppComponent {
  @Component.Factory
  interface Factory {
    fun create(@BindsInstance application: Application): ReleaseAppComponent
  }
}

fun Application.createAppComponent() = DaggerReleaseAppComponent.factory().create(this)
