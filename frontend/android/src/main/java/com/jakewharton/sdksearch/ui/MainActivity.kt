package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.DbComponent
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MainActivity : Activity() {
  private lateinit var disposable: Disposable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT == 26) {
      // This attribute can only be set in code on API 26. It's in the theme in 27+.
      window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    if ("true" == intent.getStringExtra("crash")) {
      Timber.e("Synthetic crash signal detected. Throwing in 3.. 2.. 1..")
      throw RuntimeException("Crash! Bang! Pow! This is only a test...")
    }

    val baseUrl = BaseUrl(PRODUCTION_DAC)

    val service = DacComponent.builder()
        .baseUrl(baseUrl)
        .build()
        .documentationService()

    val store = DbComponent.builder()
        .context(this)
        .scheduler(Schedulers.io())
        .filename("sdk.db")
        .build()
        .itemStore()

    val defaultQuery = if (savedInstanceState == null) {
      intent.getStringExtra("query")
    } else null

    val synchronizer = ItemSynchronizer(store, service, ITEM_LIST_URL_PATHS)
    val androidReference = AndroidReference(PRODUCTION_GIT_WEB)

    setContentView(R.layout.main)
    val binder = SearchViewBinder(window.decorView)
    val presenter = SearchPresenter(this, binder, defaultQuery, baseUrl, androidReference, store, synchronizer)

    disposable = presenter.start()
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.dispose()
  }
}
