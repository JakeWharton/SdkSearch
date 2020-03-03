package com.jakewharton.sdksearch.store.item

import com.chrome.platform.storage.StorageArea
import com.chrome.platform.storage.changes
import com.chrome.platform.storage.set
import com.jakewharton.sdksearch.store.item.Item.Impl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Increment the number suffix for versioning.
private const val KEY = "items_2"

// TODO replace with https://github.com/Kotlin/kotlinx.serialization/issues/116
// TODO stop golfing byte count https://bugs.chromium.org/p/chromium/issues/detail?id=863214

private fun Item.toPacked() = arrayOf<Any>(packageName, className, if (deprecated) 1 else 0, link)
private fun List<Item>.toPacked() = Array(size) { this[it].toPacked() }

private fun unpackItem(value: dynamic): Item = Impl(-1, value[0], value[1], value[2] == 1, value[3])
private fun unpackToItemList(value: dynamic) = (value as Array<Any>).map(::unpackItem)

class StorageAreaItemStore(
  private val storageArea: StorageArea
) : ItemStore {
  private val itemsFlow = storageArea.changes(KEY)
      .map { it?.let(::unpackToItemList) ?: emptyList() }

  override suspend fun updateItems(items: List<Item>) {
    storageArea.set(KEY, items.toPacked())
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
