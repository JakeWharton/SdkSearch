package com.jakewharton.sdksearch.store.item

import com.chrome.platform.storage.StorageArea
import com.chrome.platform.storage.changes
import com.chrome.platform.storage.get
import com.chrome.platform.storage.set
import com.jakewharton.sdksearch.store.item.Item.Impl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Increment the number suffix for versioning.
private const val KEY = "items_2"

// TODO replace with https://github.com/Kotlin/kotlinx.serialization/issues/116
// TODO stop golfing byte count https://bugs.chromium.org/p/chromium/issues/detail?id=863214

private fun Item.toPacked() = arrayOf<dynamic>(packageName, className, if (deprecated) 1 else 0, link)
private fun List<Item>.toPacked() = Array(size) { this[it].toPacked() }
private fun Map<*, Item>.toPacked() = values.toList().toPacked()

private fun unpackItem(value: dynamic): Item = Impl(-1, value[0], value[1], value[2] == 1, value[3])
private fun unpackToItemList(value: dynamic) = (value as Array<dynamic>).map(::unpackItem)
private fun unpackToMutableItemMap(value: dynamic): MutableMap<String, Item> {
  return unpackToItemList(value).associateByTo(mutableMapOf(), Item::fqcn)
}

private val Item.fqcn get() = "$packageName.$className"

class StorageAreaItemStore(
  private val storageArea: StorageArea
) : ItemStore {
  private var currentJob: Job? = null
  private val itemsFlow = storageArea.changes(KEY)
      .map { it?.let(::unpackToItemList) ?: emptyList() }

  private suspend fun <T> serially(body: suspend () -> T): T {
    // TODO replace with actor once available in JS.

    while (true) {
      currentJob?.join() ?: break
    }
    val job = GlobalScope.async {
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
      val existingItems = storageArea.get(KEY)

      val newItems = if (existingItems == null) {
        items.toPacked()
      } else {
        val mutableMap = unpackToMutableItemMap(existingItems)
        for (item in items) {
          mutableMap[item.fqcn] = item
        }
        mutableMap.toPacked()
      }

      storageArea.set(KEY, newItems)
    }
  }

  override fun queryItems(term: String): Flow<List<Item>> {
    return itemsFlow.map { items ->
      items.filter { it.className.contains(term, ignoreCase = true) }
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
    }
  }

  override fun count(): Flow<Long> {
    return itemsFlow.map { it.size.toLong() }
  }
}
