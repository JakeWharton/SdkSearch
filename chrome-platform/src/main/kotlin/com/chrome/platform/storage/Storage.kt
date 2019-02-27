package com.chrome.platform.storage

import com.chrome.platform.events.Event
import kotlin.js.Json

/** https://developer.chrome.com/apps/storage */
external interface Storage {
  /** https://developer.chrome.com/apps/storage#property-local */
  val local: StorageArea

  /** https://developer.chrome.com/apps/storage#event-onChanged */
  // TODO can we somehow model the Json type as a String to StorageChange object?
  val onChanged: Event<(changes: Json, areaName: String) -> Unit>
}
