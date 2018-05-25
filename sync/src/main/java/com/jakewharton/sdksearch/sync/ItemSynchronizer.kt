package com.jakewharton.sdksearch.sync

import com.jakewharton.sdksearch.api.dac.DocumentationService
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.store.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer.LoaderEvent.ForceSync
import com.jakewharton.sdksearch.sync.ItemSynchronizer.LoaderEvent.LoadResult
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.IOException

class ItemSynchronizer(
  private val itemStore: ItemStore,
  private val documentationService: DocumentationService
) {

  private val _state = ConflatedChannel<SyncStatus>()
  val state: ReceiveChannel<SyncStatus> get() = _state

  private val loader = actor<LoaderEvent> {
    var status = SyncStatus.IDLE

    // TODO we probably don't need an actor anymore.
    consumeEach { event ->
      when (event) {
        is ForceSync -> {
          status = SyncStatus.SYNC
          launch { load() }
        }
        is LoadResult -> {
          status = if (event.success) SyncStatus.IDLE else SyncStatus.FAILED
        }
      }
      _state.offer(status)
    }
  }

  fun forceSync() {
    loader.offer(ForceSync)
  }

  private suspend fun load() {
    Timber.d("Listing items...")

    val result = try {
      documentationService.list().await()
    } catch (e: IOException) {
      Timber.i(e, "Unable to load items")
      loader.send(LoadResult(false))
      return
    }

    val apiItems = result.values.singleOrNull()
    if (apiItems == null) {
      Timber.w("More than one key returned from listing: ${result.keys}")
      loader.send(LoadResult(false))
      return
    }

    Timber.d("Listing got ${apiItems.size} items")

    val items = apiItems.map { Item.createForInsert(it.type, it.link, it.metadata) }
    try {
      itemStore.updateItems(items)
    } catch (e: RuntimeException) {
      Timber.i(e, "Unable to save items")
      loader.send(LoadResult(false))
      return
    }

    loader.send(LoadResult(true))
  }

  enum class SyncStatus {
    IDLE, SYNC, FAILED
  }

  private sealed class LoaderEvent {
    object ForceSync : LoaderEvent()
    data class LoadResult(val success: Boolean) : LoaderEvent()
  }
}
