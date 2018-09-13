package com.jakewharton.sdksearch

import com.jakewharton.sdksearch.options.presenter.OptionsPresenter
import com.jakewharton.sdksearch.store.config.ConfigStore
import dagger.Module
import dagger.Provides

@Module
object OptionsPresenterModule {
  @JvmStatic
  @Provides
  fun provideOptionsPresenter(configStore: ConfigStore): OptionsPresenter {
    return OptionsPresenter(configStore)
  }
}
