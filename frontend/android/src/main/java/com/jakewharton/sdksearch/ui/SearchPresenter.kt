package com.jakewharton.sdksearch.ui

import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.store.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import com.jakewharton.sdksearch.util.addTo
import com.jakewharton.sdksearch.util.crashingSubscribe
import com.jakewharton.sdksearch.util.ofType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.openSubscription
import kotlinx.coroutines.experimental.selects.select
import java.util.concurrent.TimeUnit

class SearchPresenter(
    private val onClick: ItemHandler,
    private val onCopy: ItemHandler,
    private val onShare: ItemHandler,
    private val onSource: ItemHandler,
    private val store: ItemStore,
    private val synchronizer: ItemSynchronizer
) {
  private val _models = ConflatedChannel<Model>()
  val models: ReceiveChannel<Model> get() = _models

  fun start(events: Observable<Event>): Disposable {
    val disposables = CompositeDisposable()

    events.crashingSubscribe {
      when (it) {
        is Event.ItemClick -> onClick(it.item)
        is Event.ItemCopy -> onCopy(it.item)
        is Event.ItemShare -> onShare(it.item)
        is Event.ItemViewSource -> onSource(it.item)
      }
    }.addTo(disposables)

    val itemCount = store.count().openSubscription()

    val queryItems = events
        .ofType<Event.QueryChanged>()
        .map(Event.QueryChanged::query)
        .switchMap { query ->
          val results = if (query.isBlank()) Observable.just(emptyList())
          else store.queryItems(query).delaySubscription(200, TimeUnit.MILLISECONDS, mainThread())

          results.map { Model.QueryResults(query, it) }
        }
        .openSubscription()

    val clearSyncStatus = events
        .ofType<Event.ClearSyncStatus>()
        .openSubscription()

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
          synchronizer.state.onReceive {
            val failed = it.count { it.failed }
            val inFlight = it.size - failed
            model.copy(syncStatus = Model.SyncStatus(inFlight, failed))
          }
          clearSyncStatus.onReceive {
            model.copy(syncStatus = Model.SyncStatus(0, 0))
          }
        }
        _models.send(model)
      }
    }

    synchronizer.forceSync()

    return disposables
  }

  sealed class Event {
    class ItemClick(val item: Item): Event()
    class ItemCopy(val item: Item): Event()
    class ItemShare(val item: Item): Event()
    class ItemViewSource(val item: Item): Event()
    class QueryChanged(val query: String): Event()
    object ClearSyncStatus : Event()
  }

  data class Model(
      val count: Long = 0,
      val queryResults: QueryResults = QueryResults(),
      val syncStatus: SyncStatus = SyncStatus()
  ) {
    data class QueryResults(
        val query: String = "",
        val items: List<Item> = emptyList()
    )
    data class SyncStatus(
        val inFlight: Int = 0,
        val failed: Int = 0
    )
  }
}
