package com.chrome.platform.storage

import kotlin.js.Json

/** https://developer.chrome.com/apps/storage#type-StorageArea */
external interface StorageArea {
  fun get(vararg key: String, callback: (Json) -> Unit)
  fun set(items: Json, callback: () -> Unit)
  fun remove(vararg key: String, callback: () -> Unit)
  fun clear(callback: () -> Unit)
}
