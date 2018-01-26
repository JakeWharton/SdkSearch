package com.chrome.platform.storage

import com.chrome.platform.promised
import kotlin.js.Json

/** https://developer.chrome.com/apps/storage#type-StorageArea */
external interface StorageArea {
  fun get(vararg key: String, callback: (Json) -> Unit)
  fun set(items: Json, callback: () -> Unit)
  fun remove(vararg key: String, callback: () -> Unit)
  fun clear(callback: () -> Unit)
}

fun StorageArea.get(vararg key: String) = promised<Json> { resolve ->
  get(*key, callback = resolve)
}

fun StorageArea.set(items: Json) = promised<Unit> { resolve ->
  set(items) {
    resolve(Unit)
  }
}
