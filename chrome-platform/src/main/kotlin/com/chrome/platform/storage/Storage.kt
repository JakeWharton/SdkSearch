package com.chrome.platform.storage

import com.chrome.platform.events.Event
import kotlin.js.Json

/** https://developer.chrome.com/apps/storage */
external interface Storage {
  /** https://developer.chrome.com/apps/storage#property-local */
  val local: StorageArea
}
