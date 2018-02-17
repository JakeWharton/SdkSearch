package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v7.util.DiffUtil
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.DbComponent
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import com.jakewharton.sdksearch.ui.SearchViewBinder.Event.ItemClick
import com.jakewharton.sdksearch.ui.SearchViewBinder.Event.ItemCopy
import com.jakewharton.sdksearch.ui.SearchViewBinder.Event.ItemShare
import com.jakewharton.sdksearch.ui.SearchViewBinder.Event.ItemViewSource
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.computation
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS

internal typealias ItemHandler = (Item) -> Unit

class MainActivity : Activity() {
  private val baseUrl = BaseUrl(PRODUCTION_DAC)
  private val disposables = CompositeDisposable()

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

    val synchronizer = ItemSynchronizer(store, service, ITEM_LIST_URL_PATHS)
    val androidReference = AndroidReference(PRODUCTION_GIT_WEB)

    val onClick = OpenDocumentationItemHandler(this, baseUrl, androidReference)
    val onCopy = ClipboardCopyItemHandler(this, baseUrl)
    val onShare = ShareItemHandler(this, baseUrl)
    val onSource = OpenSourceItemHandler(this, androidReference)

    setContentView(R.layout.main)
    val binder = SearchViewBinder(window.decorView)

    val recycler = binder.results
    val adapter = binder.resultsAdapter
    val queryInput = binder.queryInput

    if (savedInstanceState == null) {
      queryInput.setText(intent.getStringExtra("query") ?: "")
    }

    binder.events.crashingSubscribe {
      when (it) {
        is ItemClick -> onClick(it.item)
        is ItemCopy -> onCopy(it.item)
        is ItemShare -> onShare(it.item)
        is ItemViewSource -> onSource(it.item)
      }
    }

    store.count()
        .observeOn(mainThread())
        .crashingSubscribe {
          queryInput.hint = resources.getQuantityString(R.plurals.search_classes, it.toInt(), it)
        }

    queryInput.textChanges()
        .map(CharSequence::toString)
        .switchMap { query ->
          val results = if (query.isBlank()) just(emptyList())
          else store.queryItems(query).delaySubscription(200, MILLISECONDS, mainThread())

          results.map { query to it }
        }
        .observeOn(computation())
        .scan(QueryResults()) { (oldQuery, oldItems), (newQuery, newItems) ->
          val diff = DiffUtil.calculateDiff(ItemDiffer(oldQuery, oldItems, newQuery, newItems))
          QueryResults(newQuery, newItems, diff)
        }
        .skip(1)
        .observeOn(mainThread())
        .crashingSubscribe {
          adapter.updateItems(it.query, it.data)
          it.diff.dispatchUpdatesTo(adapter)

          // Always reset the scroll position to the top when the query changes.
          recycler.scrollToPosition(0)
        }

    launch(UI, UNDISPATCHED) {
      var snackbar: Snackbar? = null
      for (state in synchronizer.state) {
        if (state.isNotEmpty()) {
          val failed = state.count { it.failed }
          val updating = state.size - failed

          val message = when {
            failed == 0 -> {
              resources.getQuantityString(R.plurals.updating, updating, updating)
            }
            updating == 0 -> {
              resources.getQuantityString(R.plurals.updating_failed, failed, failed)
            }
            else -> {
              resources.getQuantityString(R.plurals.updating_with_failed, updating, updating, failed)
            }
          }

          if (snackbar == null) {
            snackbar = Snackbar.make(recycler, message, LENGTH_INDEFINITE)
            snackbar.show()
          } else {
            snackbar.setText(message)
          }

          if (failed > 0 && updating == 0) {
            snackbar.setAction(R.string.dismiss) {
              snackbar.dismiss()
            }
          }
        } else {
          snackbar?.dismiss()
        }
      }
    }

    synchronizer.forceSync()
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  @Suppress("NOTHING_TO_INLINE") // Needed for correct stacktraces.
  private inline fun <I> Observable<I>.crashingSubscribe(noinline onNext: (I) -> Unit) {
    disposables.add(subscribe(onNext, { throw OnErrorNotImplementedException(it) }))
  }
}
