package com.jakewharton.sdksearch.store.item

import com.chrome.platform.storage.StorageArea
import com.chrome.platform.storage.StorageChange
import com.jakewharton.sdksearch.store.item.Item.Impl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import timber.log.debug
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.json

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

// TODO maybe replace with https://github.com/JetBrains/kotlin-wrappers?
private external object Object {
  fun keys(o: Any): Array<String>
}

class StorageAreaItemStore(
  private val storageArea: StorageArea
) : ItemStore {
  private var currentJob: Job? = null
  private val itemsSink: SendChannel<List<Item>>
  private val itemsFlow: Flow<List<Item>>
  init {
    val itemsChannel = ConflatedBroadcastChannel(emptyList<Item>())
    itemsSink = itemsChannel
    itemsFlow = itemsChannel.asFlow()
  }

  init {
    storageArea.get(KEY) {
      val existingItems = it[KEY]?.let(::unpackToItemList) ?: emptyList()
      Timber.debug { "Loaded initial ${existingItems.size} items" }
      itemsSink.offer(existingItems)

      storageArea.onChanged.addListener { objects, _ ->
        Timber.debug { """Storage change of key(s) "${Object.keys(objects)}"""" }
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        val change = objects[KEY] as StorageChange?
        if (change != null) {
          itemsSink.offer(unpackToItemList(change.newValue))
        }
      }
    }
  }

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
      suspendCoroutine<Unit> { continuation ->
        storageArea.get(KEY) {
          val existingItems = it[KEY]

          val newItems = if (existingItems == null) {
            items.toPacked()
          } else {
            val mutableMap = unpackToMutableItemMap(existingItems)
            for (item in items) {
              mutableMap[item.fqcn] = item
            }
            mutableMap.toPacked()
          }

          storageArea.set(json(KEY to newItems)) {
            continuation.resume(Unit)
          }
        }
      }
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
