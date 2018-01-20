package com.jakewharton.sdksearch.store

import com.chrome.platform.storage.StorageArea
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.js.json

class StorageAreaItemStore(
  private val key: String,
  private val storage: StorageArea
) : ItemStore {
  private var currentJob: Job? = null

  private suspend fun serially(body: suspend () -> Unit) {
    // TODO replace with actor once available in JS.

    while (true) {
      currentJob?.join() ?: break
    }
    val job = launch {
      try {
        body()
      } finally {
        currentJob = null
      }
    }
    currentJob = job
    job.join()
  }

  override suspend fun updateListing(listing: String, items: List<Item>) {
    serially {
      suspendCoroutine<Unit> { continuation ->
        storage.get(key) {
          @Suppress("UNCHECKED_CAST")
          val existingItems = it[key] as Array<Item>? ?: emptyArray()
          val oldItems = existingItems.filter { it.listing != listing }
          val newItems = oldItems + items
          storage.set(json(key to newItems.toTypedArray())) {
            continuation.resume(Unit)
          }
        }
      }
    }
  }
}
