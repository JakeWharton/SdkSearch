package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.SdkSearchApplication
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import kotlinx.coroutines.experimental.Job
import timber.log.Timber

class MainActivity : Activity() {
  private lateinit var presenterJob: Job
  private lateinit var binderJob: Job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT == 26 && resources.getBoolean(R.bool.light_navigation)) {
      // This attribute can only be set in code on API 26. It's in the theme in 27+.
      window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    if ("true" == intent.getStringExtra("crash")) {
      Timber.e("Synthetic crash signal detected. Throwing in 3.. 2.. 1..")
      throw RuntimeException("Crash! Bang! Pow! This is only a test...")
    }

    val app = application as SdkSearchApplication

    val presenter = app.presenter
    presenterJob = lastNonConfigurationInstance as Job? ?: presenter.start()

    val baseUrl = app.baseUrl
    val androidReference = AndroidReference(PRODUCTION_GIT_WEB)
    val onClick = OpenDocumentationItemHandler(this, baseUrl, androidReference)
    val onCopy = ClipboardCopyItemHandler(this, baseUrl)
    val onShare = ShareItemHandler(this, baseUrl)
    val onSource = OpenSourceItemHandler(this, androidReference)

    val defaultQuery = if (savedInstanceState == null) {
      SearchViewBinder.Args(intent.getStringExtra("query"))
    } else null

    setContentView(R.layout.main)
    val binder = SearchViewBinder(window.decorView, onClick, onCopy, onShare, onSource)
    defaultQuery?.let { binder.init(it) }
    binderJob = binder.attach(presenter.models, presenter.events)
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
