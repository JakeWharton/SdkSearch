package com.jakewharton.sdksearch.sync

import com.jakewharton.sdksearch.api.dac.DocumentationService
import com.jakewharton.sdksearch.store.ItemStore
import com.jakewharton.sdksearch.store.ItemUtil
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.IOException

class ItemSynchronizer(
  private val itemStore: ItemStore,
  private val documentationService: DocumentationService
) {
  private val _state = ConflatedChannel<SyncStatus>()
  val state: ReceiveChannel<SyncStatus> get() = _state

  fun forceSync() {
    _state.offer(SyncStatus.SYNC)
    launch {
      val result = if (load()) SyncStatus.IDLE else SyncStatus.FAILED
      _state.offer(result)
    }
  }

  private suspend fun load(): Boolean {
    Timber.d("Listing items...")

    val result = try {
      documentationService.list().await()
    } catch (e: IOException) {
      Timber.i(e, "Unable to load items")
      return false
    }

    val apiItems = result.values.singleOrNull()
    if (apiItems == null) {
      Timber.w("More than one key returned from listing: ${result.keys}")
      return false
    }

    Timber.d("Listing got ${apiItems.size} items")

    val items = apiItems.map { ItemUtil.createForInsert(it.type, it.link, it.metadata) }
    try {
      itemStore.updateItems(items)
    } catch (e: RuntimeException) {
      Timber.i(e, "Unable to save items")
      return false
    }

    return true
  }

  enum class SyncStatus {
    IDLE, SYNC, FAILED
  }
}
