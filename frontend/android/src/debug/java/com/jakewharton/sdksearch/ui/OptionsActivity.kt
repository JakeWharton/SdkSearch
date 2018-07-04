package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Bundle
import com.jakewharton.pbandk.Location
import com.jakewharton.pbandk.bindTo
import com.jakewharton.pbandk.startLocation
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter
import com.jakewharton.sdksearch.options.ui.OptionsUiBinder
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.experimental.Job
import javax.inject.Inject
import javax.inject.Provider

class OptionsActivity : Activity() {
  @Inject lateinit var optionsPresenterProvider: Provider<OptionsPresenter>

  private lateinit var location: Location
  private lateinit var binderJob: Job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)

    location = lastNonConfigurationInstance as Location?
        ?: optionsPresenterProvider.get().startLocation()
    val presenter = location.presenter as OptionsPresenter

    setContentView(R.layout.options)
    val binder = OptionsUiBinder(window.decorView, presenter.events)

    binderJob = binder.bindTo(presenter)
  }

  override fun onRetainNonConfigurationInstance() = location

  override fun onDestroy() {
    super.onDestroy()
    binderJob.cancel()

    if (!isChangingConfigurations) {
      location.stop()
    }
  }
}

@Module
abstract class OptionsActivityModule {
  @ContributesAndroidInjector
  abstract fun contributeOptionsActivity(): OptionsActivity
}
