package com.jakewharton.sdksearch.sync

import android.support.annotation.MainThread
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

@MainThread
class ItemSynchronizer(
  private val itemStore: ItemStore,
  private val documentationService: DocumentationService,
  private val references: Map<String, String>
) {

  private val _state = ConflatedChannel<List<SyncStatus>>()
  val state: ReceiveChannel<List<SyncStatus>> get() = _state

  private val loader = actor<LoaderEvent> {
    val status = mutableListOf<SyncStatus>()

    consumeEach { event ->
      when (event) {
        is ForceSync -> {
          status.clear()
          status.addAll(references.keys.map { SyncStatus(it) })

          references.forEach { (listing, url) ->
            launch {
              load(listing, url)
            }
          }
        }
        is LoadResult -> {
          if (event.success) {
            status.removeIf { it.name == event.listing }
          } else {
            status.updateIf({ it.name == event.listing }, { it.copy(failed = true) })
          }
        }
      }
      _state.offer(status)
    }
  }

  fun forceSync() {
    loader.offer(ForceSync)
  }

  private suspend fun load(listing: String, url: String) {
    Timber.d("Listing $listing...")

    val apiItems = try {
      documentationService.list(url).await()
    } catch (e: IOException) {
      Timber.i(e, "Unable to load $listing")
      loader.send(LoadResult(listing, false))
      return
    }
    Timber.d("Listing $listing got ${apiItems.size} items")

    val items = apiItems
        .filter { it.type == "class" }
        .map { Item.createForInsert(it.label, it.link, it.deprecated) }

    try {
      itemStore.updateItems(items)
    } catch (e: RuntimeException) {
      Timber.i(e, "Unable to save $listing")
      loader.send(LoadResult(listing, false))
    }

    loader.send(LoadResult(listing, true))
  }

  data class SyncStatus(val name: String, val failed: Boolean = false)

  private sealed class LoaderEvent {
    object ForceSync : LoaderEvent()
    data class LoadResult(val listing: String, val success: Boolean) : LoaderEvent()
  }
}

private fun <T> MutableList<T>.updateIf(selector: (T) -> Boolean, operation: (T) -> T) {
  for (index in indices) {
    val item = get(index)
    if (selector(item)) {
      set(index, operation(item))
    }
  }
}
