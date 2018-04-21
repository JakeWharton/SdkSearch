package com.jakewharton.sdksearch.debug.updater

import android.app.Activity
import android.os.Bundle
import android.support.annotation.RestrictTo
import android.support.annotation.RestrictTo.Scope.LIBRARY
import androidx.core.widget.toast
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.ContributesAndroidInjector
import javax.inject.Inject

@RestrictTo(LIBRARY) // Public for Android to create.
class UpdateActivity : Activity() {
  @Inject lateinit var config: UpdateConfig

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)

    if (config.apiToken.isBlank()) {
      toast("Circle CI token missing. Cannot update!")
    } else {
      startUpdateService(config)
    }

    finish()
  }
}

@Module
abstract class UpdateActivityModule {
  @ContributesAndroidInjector
  abstract fun contributeUpdateActivity(): UpdateActivity
}
