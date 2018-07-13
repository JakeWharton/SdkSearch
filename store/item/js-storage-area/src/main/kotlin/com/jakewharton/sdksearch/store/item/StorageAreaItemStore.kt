package com.jakewharton.sdksearch.store.item

import com.chrome.platform.storage.Storage
import com.chrome.platform.storage.StorageArea
import com.chrome.platform.storage.StorageChange
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
private fun List<Item>.toJsArray(): dynamic {
  val array = js("[]")
  for (item in this) {
    val js = js("[]")
    js.push(item.packageName)
    js.push(item.className)
    js.push(if (item.deprecated) 1 else 0)
    js.push(item.link)
    array.push(js)
  }
  return array
}

// TODO replace with https://github.com/Kotlin/kotlinx.serialization/issues/116
// TODO stop golfing byte count https://bugs.chromium.org/p/chromium/issues/detail?id=863214
private fun fromJsArray(value: dynamic): List<Item> {
  val list = mutableListOf<Item>()
  for (item in value) {
    list.add(Item.Impl(
        -1,
        item[0],
        item[1],
        item[2] == 1,
        item[3]
    ))
  }
  return list
}

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
  private val items = ConflatedBroadcastChannel<List<Item>?>(null)
  private var currentJob: Job? = null

  init {
    storage.onChanged.addListener { objects, name ->
      Timber.debug { """Storage change in "$name" of key(s) "${Object.keys(objects)}"""" }
      if (name == storageName) {
        objects[KEY]?.let { (it as StorageChange).newValue }?.let(::fromJsArray)?.let(items::offer)
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
          val existingItems = it[KEY]?.let(::fromJsArray) ?: emptyList()
          // TODO something not n^2? persist map of fqcn to item?
          val oldItems = existingItems.filter { existing ->
            items.any { it.packageName == existing.packageName && it.className == existing.className }
          }
          val newItems = oldItems + items
          storageArea.set(json(KEY to newItems.toJsArray())) {
            continuation.resume(Unit)
          }
        }
      }
    }
  }

  override fun queryItems(term: String): ReceiveChannel<List<Item>> {
    return items.openSubscription().map {
      (it ?: emptyList())
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
    }
  }

  override fun count(): ReceiveChannel<Long> {
    return items.openSubscription().map { it?.size?.toLong() ?: 0L }
  }
}
