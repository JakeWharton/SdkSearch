package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Bundle
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter
import com.jakewharton.sdksearch.options.ui.OptionsUiBinder
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class OptionsActivity : Activity() {
  @Inject lateinit var presenter: OptionsPresenter

  private lateinit var presenterJob: Job
  private lateinit var binderJob: Job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)

    presenterJob = lastNonConfigurationInstance as Job? ?: presenter.start()

    setContentView(R.layout.options)
    val binder = OptionsUiBinder(window.decorView, presenter.events)

    binderJob = launch(Unconfined) {
      var oldModel: OptionsPresenter.Model? = null
      for (model in presenter.models) {
        binder.bind(model, oldModel)
        oldModel = model
      }
    }
  }

  override fun onRetainNonConfigurationInstance() = presenterJob

  override fun onDestroy() {
    super.onDestroy()
    binderJob.cancel()

    if (!isChangingConfigurations) {
      presenterJob.cancel()
    }
  }
}

@Module
abstract class OptionsActivityModule {
  @ContributesAndroidInjector
  abstract fun contributeOptionsActivity(): OptionsActivity
}
