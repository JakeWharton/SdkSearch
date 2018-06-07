package com.jakewharton.sdksearch

import android.app.Application
import android.content.Context.MODE_PRIVATE
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.config.ConfigStore
import com.jakewharton.sdksearch.store.config.SharedPreferencesConfigStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ConfigStoreModule {
  @JvmStatic
  @Provides
  @Singleton
  fun provideConfigStore(application: Application): ConfigStore {
    val preferences = application.getSharedPreferences("config", MODE_PRIVATE)
    return SharedPreferencesConfigStore(preferences, PRODUCTION_GIT_WEB, PRODUCTION_DAC)
  }
}
