package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.store.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import com.jakewharton.sdksearch.ui.SearchViewBinder.Model
import com.jakewharton.sdksearch.ui.SearchViewBinder.Model.QueryResults
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
import java.util.concurrent.TimeUnit

internal class SearchPresenter(
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
        is SearchViewBinder.Event.ItemClick -> onClick(it.item)
        is SearchViewBinder.Event.ItemCopy -> onCopy(it.item)
        is SearchViewBinder.Event.ItemShare -> onShare(it.item)
        is SearchViewBinder.Event.ItemViewSource -> onSource(it.item)
      }
    }.addTo(disposables)

    val models = Channel<Model>()

    val itemCount = store.count()
        .observeOn(AndroidSchedulers.mainThread())
        .startWith(0L)

    val queryItems = binder.events
        .ofType<SearchViewBinder.Event.QueryChanged>()
        .map(SearchViewBinder.Event.QueryChanged::query)
        .switchMap { query ->
          val results = if (query.isBlank()) Observable.just(emptyList())
          else store.queryItems(query).delaySubscription(200, TimeUnit.MILLISECONDS, mainThread())

          results.map { QueryResults(query, it) }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .startWith(QueryResults("", emptyList()))

    val items = Observable.combineLatest(itemCount, queryItems,
        BiFunction<Long, QueryResults, Model> { count, queryResults -> Model(count, queryResults) })

    items.crashingSubscribe {
      models.offer(it)
    }.addTo(disposables)

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
}
