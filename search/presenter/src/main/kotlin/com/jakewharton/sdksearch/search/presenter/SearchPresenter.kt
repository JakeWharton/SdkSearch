package com.jakewharton.sdksearch.search.presenter

import com.jakewharton.presentation.Presenter
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model.SyncStatus
import com.jakewharton.sdksearch.store.item.Item
import com.jakewharton.sdksearch.store.item.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class SearchPresenter(
  private val store: ItemStore,
  private val synchronizer: ItemSynchronizer,
  /** The delay (in milliseconds) before the result query for an input occurs. */
  private val queryDelay: Long = 200L
) : Presenter<Model, Event> {
  private val _models = ConflatedBroadcastChannel<Model>()
  override val models: ReceiveChannel<Model> get() = _models.openSubscription()

  private val _events = RendezvousChannel<Event>()
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
