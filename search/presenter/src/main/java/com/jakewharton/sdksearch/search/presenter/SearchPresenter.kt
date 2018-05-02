package com.jakewharton.sdksearch.search.presenter

import com.jakewharton.rxrelay2.PublishRelay
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

class SearchPresenter(
  private val context: CoroutineDispatcher,
  private val store: ItemStore,
  private val synchronizer: ItemSynchronizer
) {
  private val _models = ConflatedBroadcastChannel<Model>()
  val models: ReceiveChannel<Model> get() = _models.openSubscription()

  val _events = PublishRelay.create<Event>()

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
            val failed = it.count { it.failed }
            val inFlight = it.size - failed
            model.copy(syncStatus = Model.SyncStatus(inFlight, failed))
          }
          clearSyncStatus.onReceive {
            model.copy(syncStatus = Model.SyncStatus(0, 0))
          }
        }
        _models.offer(model)
      }
    }

    synchronizer.forceSync()

    return job
  }

  sealed class Event {
    @SuppressWarnings("SyntheticAccessor") // TODO https://issuetracker.google.com/issues/78144888
    data class QueryChanged(val query: String) : Event()
    @SuppressWarnings("SyntheticAccessor") // TODO https://issuetracker.google.com/issues/78144888
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

  private inline fun <reified T> Observable<*>.ofType(): Observable<T> = ofType(T::class.java)
}
