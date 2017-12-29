package com.jakewharton.sdksearch.sync

import android.support.annotation.MainThread
import com.jakewharton.sdksearch.api.dac.DocumentationService
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.db.ItemStore
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber
import java.io.IOException

@MainThread
class ItemSynchronizer(
  private val itemStore: ItemStore,
  private val documentationService: DocumentationService,
  private val references: Map<String, String>
) {

  private val _isRunning = ConflatedChannel<Boolean>()
  val isRunning: ReceiveChannel<Boolean> get() = _isRunning

  private val loader = actor<Unit> {
    consumeEach {
      _isRunning.offer(true)
      val success = references
          .map { (listing, url) ->
            async {
              load(listing, url)
            }
          }
          .map { it.await() }
          .all { it }
      _isRunning.offer(false)

      // TODO propagate results
      Timber.i("Sync status: $success")
    }
  }

  fun forceSync() {
    loader.offer(Unit)
  }

  private suspend fun load(listing: String, url: String): Boolean {
    Timber.d("Listing $listing...")

    val apiItems = try {
      documentationService.list(url).await()
    } catch (e: IOException) {
      Timber.i(e, "Unable to load $listing")
      return false
    }
    Timber.d("Listing $listing got ${apiItems.size} items")

    val items = apiItems
        .filter { it.type == "class" }
        .map { Item.createForInsert(listing, it.label, it.link, it.deprecated) }

    try {
      itemStore.updateListing(listing, items)
    } catch (e: RuntimeException) {
      Timber.i(e, "Unable to save $listing")
      return false
    }

    return true
  }
}
