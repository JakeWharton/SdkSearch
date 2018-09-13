package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Bundle
import com.jakewharton.presentation.Presentation
import com.jakewharton.presentation.bindTo
import com.jakewharton.presentation.startPresentation
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter
import com.jakewharton.sdksearch.options.ui.OptionsUiBinder
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import javax.inject.Inject
import javax.inject.Provider

class OptionsActivity : Activity() {
  @Inject lateinit var optionsPresenterProvider: Provider<OptionsPresenter>

  private lateinit var presentation: Presentation
  private lateinit var binderJob: Job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)

    presentation = lastNonConfigurationInstance as Presentation?
        ?: optionsPresenterProvider.get().startPresentation(Dispatchers.Main)
    val presenter = presentation.presenter as OptionsPresenter

    setContentView(R.layout.options)
    val binder = OptionsUiBinder(window.decorView, presenter.events)

    binderJob = binder.bindTo(presenter)
  }

  override fun onRetainNonConfigurationInstance() = presentation

  override fun onDestroy() {
    super.onDestroy()
    binderJob.cancel()

    if (!isChangingConfigurations) {
      presentation.stop()
    }
  }
}

@Module
abstract class OptionsActivityModule {
  @ContributesAndroidInjector
  abstract fun contributeOptionsActivity(): OptionsActivity
}
