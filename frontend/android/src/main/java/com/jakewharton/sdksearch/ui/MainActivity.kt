package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.search.ui.SearchUiBinder
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

class MainActivity : Activity() {
  @Inject lateinit var presenter: SearchPresenter
  @Inject lateinit var baseUrl: BaseUrl

  private lateinit var presenterJob: Job
  private lateinit var binderJob: Job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT == 26 && resources.getBoolean(R.bool.light_navigation)) {
      // This attribute can only be set in code on API 26. It's in the theme in 27+.
      window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    if ("true" == intent.getStringExtra("crash")) {
      Timber.error { "Synthetic crash signal detected. Throwing in 3.. 2.. 1.." }
      throw RuntimeException("Crash! Bang! Pow! This is only a test...")
    }

    AndroidInjection.inject(this)

    presenterJob = lastNonConfigurationInstance as Job? ?: presenter.start()

    val androidReference = AndroidReference(PRODUCTION_GIT_WEB, PRODUCTION_DAC)
    val onClick = OpenDocumentationItemHandler(this, baseUrl, androidReference)
    val onCopy = ClipboardCopyItemHandler(this, baseUrl)
    val onShare = ShareItemHandler(this, baseUrl)
    val onSource = OpenSourceItemHandler(this, androidReference)

    val defaultQuery = if (savedInstanceState == null) {
      SearchUiBinder.Args(intent.getStringExtra("query"))
    } else null

    setContentView(R.layout.search)
    val binder = SearchUiBinder(window.decorView, presenter.events, onClick, onCopy, onShare, onSource)
    defaultQuery?.let { binder.init(it) }

    binderJob = launch(Unconfined) {
      var oldModel: SearchPresenter.Model? = null
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
abstract class MainActivityModule {
  @ContributesAndroidInjector
  abstract fun contributeMainActivity(): MainActivity
}
