package com.jakewharton.sdksearch.sync

import com.jakewharton.sdksearch.api.dac.DocumentationService
import com.jakewharton.sdksearch.store.item.Item
import com.jakewharton.sdksearch.store.item.ItemStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.debug
import timber.log.info

class ItemSynchronizer(
  private val itemStore: ItemStore,
  private val documentationService: DocumentationService
) {
  private val _state = ConflatedBroadcastChannel<SyncStatus>()
  val state: ReceiveChannel<SyncStatus> get() = _state.openSubscription()

  fun forceSync() {
    _state.offer(SyncStatus.SYNC)
    GlobalScope.launch {
      val result = if (load()) SyncStatus.IDLE else SyncStatus.FAILED
      _state.offer(result)
    }
  }

  private suspend fun load(): Boolean {
    Timber.debug { "Listing items..." }

    val apiItems = try {
      documentationService.list()
    } catch (e: Exception) {
      Timber.info(e) { "Unable to load items" }
      return false
    }

    Timber.debug { "Listing got ${apiItems.size} items" }

    val dbItems = apiItems
        .map { Item.Impl(-1, it.packageName, it.className, it.deprecated, it.link) }
    try {
      itemStore.updateItems(dbItems)
    } catch (e: RuntimeException) {
      Timber.info(e) { "Unable to save items" }
      return false
    }

    return true
  }

  enum class SyncStatus {
    IDLE, SYNC, FAILED
  }
}
