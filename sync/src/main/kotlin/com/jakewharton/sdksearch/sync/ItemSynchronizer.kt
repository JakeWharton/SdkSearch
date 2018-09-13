package com.jakewharton.sdksearch.sync

import com.jakewharton.sdksearch.api.dac.DocumentationService
import com.jakewharton.sdksearch.store.item.ItemStore
import com.jakewharton.sdksearch.store.item.ItemUtil
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import timber.log.debug
import timber.log.info
import timber.log.warn

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

    val result = try {
      documentationService.list().await()
    } catch (e: Exception) {
      Timber.info(e) { "Unable to load items" }
      return false
    }

    val apiItems = result.values.singleOrNull()
    if (apiItems == null) {
      Timber.warn { "More than one key returned from listing: ${result.keys}" }
      return false
    }

    Timber.debug { "Listing got ${apiItems.size} items" }

    val items = apiItems.map { ItemUtil.createForInsert(it.type, it.link, it.metadata) }
    try {
      itemStore.updateItems(items)
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
