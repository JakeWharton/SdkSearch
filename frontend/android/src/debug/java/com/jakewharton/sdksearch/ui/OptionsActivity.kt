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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class OptionsActivity : Activity() {
  private val binderJob = Job()
  private val scope = CoroutineScope(Dispatchers.Main + binderJob)

  @Inject lateinit var optionsPresenterProvider: Provider<OptionsPresenter>

  private lateinit var presentation: Presentation

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)

    presentation = lastNonConfigurationInstance as Presentation?
        ?: optionsPresenterProvider.get().startPresentation(Dispatchers.Main)
    val presenter = presentation.presenter as OptionsPresenter

    setContentView(R.layout.options)

    scope.launch(Dispatchers.Unconfined) {
      OptionsUiBinder(window.decorView, presenter.events).bindTo(presenter)
    }
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
