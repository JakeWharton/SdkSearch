package com.jakewharton.sdksearch

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class SdkSearchApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    }
  }
}
