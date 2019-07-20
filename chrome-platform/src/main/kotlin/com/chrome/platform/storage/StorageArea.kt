package com.chrome.platform.storage

import com.chrome.platform.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Json
import kotlin.js.json

/** https://developer.chrome.com/apps/storage#type-StorageArea */
external interface StorageArea {
  @JsName("get")
  fun getAsync(vararg key: String, callback: (Json) -> Unit)
  @JsName("set")
  fun setAsync(items: Json, callback: () -> Unit)

  /** https://developers.chrome.com/extensions/storage#event-StorageArea-onChanged */
  // TODO can we somehow model the Json type as a String to StorageChange object?
  val onChanged: Event<(changes: Json, areaName: String) -> Unit>
}

suspend fun StorageArea.get(key: String): Any? = suspendCoroutine { c ->
  getAsync(key) {
    c.resume(it[key])
  }
}

suspend fun StorageArea.set(key: String, value: Any?): Unit = suspendCoroutine { c ->
  setAsync(json(key to value)) {
    c.resume(Unit)
  }
}
