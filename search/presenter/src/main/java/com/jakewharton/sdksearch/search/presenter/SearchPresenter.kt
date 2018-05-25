package com.jakewharton.sdksearch.search.presenter

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model.SyncStatus
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.store.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import io.reactivex.Observable
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.openSubscription
import kotlinx.coroutines.experimental.selects.select
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class SearchPresenter(
  private val context: CoroutineDispatcher,
  private val store: ItemStore,
  private val synchronizer: ItemSynchronizer
) {
  private val _models = ConflatedBroadcastChannel<Model>()
  val models: ReceiveChannel<Model> get() = _models.openSubscription()

  private val _events = PublishRelay.create<Event>()
  val events: Consumer<Event> get() = Consumer { _events.accept(it) }

  fun start(): Job {
    val itemCount = store.count().openSubscription()

    val queryItems = _events
        .ofType<Event.QueryChanged>()
        .map(Event.QueryChanged::query)
        .distinctUntilChanged()
        .switchMap { query ->
          val results = if (query.isBlank()) Observable.just(emptyList())
          else store.queryItems(query).delaySubscription(200, TimeUnit.MILLISECONDS)

          results.map { Model.QueryResults(query, it) }
        }
        .openSubscription()

    val clearSyncStatus = _events
        .ofType<Event.ClearSyncStatus>()
        .openSubscription()

    val job = launch(context, UNDISPATCHED) {
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
            model.copy(syncStatus = when (it) {
              ItemSynchronizer.SyncStatus.IDLE -> SyncStatus.IDLE
              ItemSynchronizer.SyncStatus.SYNC -> SyncStatus.SYNC
              ItemSynchronizer.SyncStatus.FAILED -> SyncStatus.FAILED
            })
          }
          clearSyncStatus.onReceive {
            model.copy(syncStatus = SyncStatus.IDLE)
          }
        }
        _models.offer(model)
      }
    }

    synchronizer.forceSync()

    return job
  }

  sealed class Event {
    data class QueryChanged(val query: String) : Event()
    object ClearSyncStatus : Event()
  }

  data class Model(
    val count: Long = 0,
    val queryResults: QueryResults = QueryResults(),
    val syncStatus: SyncStatus = SyncStatus.IDLE
  ) {
    data class QueryResults(
      val query: String = "",
      val items: List<Item> = emptyList()
    )
    enum class SyncStatus {
      IDLE, SYNC, FAILED
    }
  }

  private inline fun <reified T> Observable<*>.ofType(): Observable<T> = ofType(T::class.java)
}
