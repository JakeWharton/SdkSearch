package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.store.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import com.jakewharton.sdksearch.util.addTo
import com.jakewharton.sdksearch.util.crashingSubscribe
import com.jakewharton.sdksearch.util.ofType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.openSubscription
import kotlinx.coroutines.experimental.selects.select
import java.util.concurrent.TimeUnit

class SearchPresenter(
    private val activity: Activity,
    private val binder: SearchViewBinder, // TODO tease this out
    private val defaultQuery: String?,
    private val onClick: ItemHandler,
    private val onCopy: ItemHandler,
    private val onShare: ItemHandler,
    private val onSource: ItemHandler,
    private val store: ItemStore,
    private val synchronizer: ItemSynchronizer
) {
  fun start(): Disposable {
    val disposables = CompositeDisposable()
    val resources = activity.resources
    val recycler = binder.results

    binder.events.crashingSubscribe {
      when (it) {
        is Event.ItemClick -> onClick(it.item)
        is Event.ItemCopy -> onCopy(it.item)
        is Event.ItemShare -> onShare(it.item)
        is Event.ItemViewSource -> onSource(it.item)
      }
    }.addTo(disposables)

    val itemCount = store.count().openSubscription()

    val queryItems = binder.events
        .ofType<Event.QueryChanged>()
        .map(Event.QueryChanged::query)
        .switchMap { query ->
          val results = if (query.isBlank()) Observable.just(emptyList())
          else store.queryItems(query).delaySubscription(200, TimeUnit.MILLISECONDS, mainThread())

          results.map { Model.QueryResults(query, it) }
        }
        .openSubscription()

    val models = Channel<Model>()
    launch(UI, UNDISPATCHED) {
      var model = Model()
      while (isActive) {
        model = select {
          itemCount.onReceive {
            model.copy(count = it)
          }
          queryItems.onReceive {
            model.copy(queryResults = it)
          }
        }
        models.send(model)
      }
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

    binder.attach(SearchViewBinder.Args(defaultQuery), models)

    synchronizer.forceSync()

    return disposables
  }

  sealed class Event {
    class ItemClick(val item: Item): Event()
    class ItemCopy(val item: Item): Event()
    class ItemShare(val item: Item): Event()
    class ItemViewSource(val item: Item): Event()
    class QueryChanged(val query: String): Event()
  }

  data class Model(
      val count: Long = 0,
      val queryResults: QueryResults = QueryResults()
  ) {
    data class QueryResults(
        val query: String = "",
        val items: List<Item> = emptyList()
    )
  }
}
