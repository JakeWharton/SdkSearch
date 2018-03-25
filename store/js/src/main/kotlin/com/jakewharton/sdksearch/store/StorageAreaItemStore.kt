package com.jakewharton.sdksearch.store

import com.chrome.platform.storage.StorageArea
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.js.json

private const val KEY = "items"

class StorageAreaItemStore(private val storage: StorageArea) : ItemStore {
  private var currentJob: Job? = null

  private suspend fun <T> serially(body: suspend () -> T): T {
    // TODO replace with actor once available in JS.

    while (true) {
      currentJob?.join() ?: break
    }
    val job = async {
      try {
        body()
      } finally {
        currentJob = null
      }
    }
    currentJob = job
    return job.await()
  }

  override suspend fun updateItems(items: List<Item>) {
    serially {
      suspendCoroutine<Unit> { continuation ->
        storage.get(KEY) {
          @Suppress("UNCHECKED_CAST")
          val existingItems = it[KEY] as Array<Item>? ?: emptyArray()
          // TODO something not n^2? persist map of fqcn to item?
          val oldItems = existingItems.filter { existing ->
            items.any { it.packageName == existing.packageName && it.className == existing.className }
          }
          val newItems = oldItems + items
          storage.set(json(KEY to newItems.toTypedArray())) {
            continuation.resume(Unit)
          }
        }
      }
    }
  }

  override suspend fun queryItems(term: String) = suspendCoroutine<List<Item>> { continuation ->
    storage.get(KEY) {
      @Suppress("UNCHECKED_CAST")
      val allItems = it[KEY] as Array<Item>? ?: emptyArray()

      val items = allItems
          .filter { it.className.contains(term, ignoreCase = true) }
          .sortedWith(compareBy {
            val name = it.className
            when {
              name.equals(term, ignoreCase = true) -> 1
              name.startsWith(term, ignoreCase = true) && name.indexOf('.') == -1 -> 2
              name.endsWith(".$term", ignoreCase = true) -> 3
              name.startsWith(term, ignoreCase = true) -> 4
              name.contains(".$term", ignoreCase = true) -> 5
              else -> 6
            }
          })
      continuation.resume(items)
    }
  }

  override suspend fun count() = suspendCoroutine<Int> { continuation ->
    storage.get(KEY) {
      @Suppress("UNCHECKED_CAST")
      val allItems = it[KEY] as Array<Item>? ?: emptyArray()

      continuation.resume(allItems.size)
    }
  }
}
