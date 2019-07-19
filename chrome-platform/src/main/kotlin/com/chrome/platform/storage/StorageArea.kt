package com.chrome.platform.storage

import com.chrome.platform.events.Event
import kotlin.js.Json

/** https://developer.chrome.com/apps/storage#type-StorageArea */
external interface StorageArea {
  fun get(vararg key: String, callback: (Json) -> Unit)
  fun get(keysAndDefaults: Json, callback: (Json) -> Unit)
  fun set(items: Json, callback: () -> Unit)
  fun remove(vararg key: String, callback: () -> Unit)
  fun clear(callback: () -> Unit)

  /** https://developers.chrome.com/extensions/storage#event-StorageArea-onChanged */
  // TODO can we somehow model the Json type as a String to StorageChange object?
  val onChanged: Event<(changes: Json, areaName: String) -> Unit>
}
