package com.jakewharton.sdksearch.store.item

import com.chrome.platform.storage.Storage
import com.chrome.platform.storage.StorageArea
import com.chrome.platform.storage.StorageChange
import com.jakewharton.sdksearch.store.item.Item.Impl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map
import timber.log.Timber
import timber.log.debug
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.js.json

private const val KEY = "items"

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

// TODO three parameters is stupid https://bugs.chromium.org/p/chromium/issues/detail?id=863277
class StorageAreaItemStore(
  storage: Storage,
  storageName: String,
  private val storageArea: StorageArea
) : ItemStore {
  private val items = ConflatedBroadcastChannel<List<Item>>()
  private var currentJob: Job? = null

  init {
    storageArea.get(KEY) {
      val existingItems = it[KEY]?.let(::unpackToItemList) ?: emptyList()
      Timber.debug { "Loaded initial ${existingItems.size} items" }
      items.offer(existingItems)

      storage.onChanged.addListener { objects, name ->
        Timber.debug { """Storage change in "$name" of key(s) "${Object.keys(objects)}"""" }
        if (name == storageName) {
          @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
          val change = objects[KEY] as StorageChange?
          if (change != null) {
            items.offer(unpackToItemList(change.newValue))
          }
        }
      }
    }
  }

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

  override fun queryItems(term: String): ReceiveChannel<List<Item>> {
    return items.openSubscription().map {
      it.filter { it.className.contains(term, ignoreCase = true) }
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

  override fun count(): ReceiveChannel<Long> {
    return items.openSubscription().map { it.size.toLong() }
  }
}
