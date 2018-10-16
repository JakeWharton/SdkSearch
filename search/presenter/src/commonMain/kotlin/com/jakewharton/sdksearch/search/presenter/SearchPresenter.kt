package com.jakewharton.sdksearch.search.presenter

import com.jakewharton.presentation.Presenter
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model.SyncStatus
import com.jakewharton.sdksearch.store.item.Item
import com.jakewharton.sdksearch.store.item.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchPresenter(
  private val store: ItemStore,
  private val synchronizer: ItemSynchronizer,
  /** The delay (in milliseconds) before the result query for an input occurs. */
  private val queryDelay: Long = 200L
) : Presenter<Model, Event> {
  private val _models = ConflatedBroadcastChannel<Model>()
  override val models: ReceiveChannel<Model> get() = _models.openSubscription()

  private val _events = Channel<Event>(RENDEZVOUS)
  override val events: SendChannel<Event> get() = _events

  override suspend fun start() = coroutineScope {
    var model = Model()
    fun sendModel(newModel: Model) {
      model = newModel
      _models.offer(newModel)
    }

    launch {
      store.count().consumeEach {
        sendModel(model.copy(count = it))
      }
    }

    launch {
      synchronizer.state.consumeEach {
        sendModel(model.copy(syncStatus = when (it) {
          ItemSynchronizer.SyncStatus.IDLE -> SyncStatus.IDLE
          ItemSynchronizer.SyncStatus.SYNC -> SyncStatus.SYNC
          ItemSynchronizer.SyncStatus.FAILED -> SyncStatus.FAILED
        }))
      }
    }

    launch {
      var activeQuery = ""
      var activeQueryJob: Job? = null

      _events.consumeEach {
        when (it) {
          is Event.ClearSyncStatus -> {
            sendModel(model.copy(syncStatus = SyncStatus.IDLE))
          }
          is Event.QueryChanged -> {
            val query = it.query
            if (query != activeQuery) {
              activeQuery = query
              activeQueryJob?.cancel()

              if (query == "") {
                sendModel(model.copy(queryResults = Model.QueryResults("", emptyList())))
              } else {
                activeQueryJob = launch {
                  delay(queryDelay)

                  store.queryItems(query).consumeEach {
                    sendModel(model.copy(queryResults = Model.QueryResults(activeQuery, it)))
                  }
                }
              }
            }
          }
        }
      }
    }

    synchronizer.forceSync()
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
}
