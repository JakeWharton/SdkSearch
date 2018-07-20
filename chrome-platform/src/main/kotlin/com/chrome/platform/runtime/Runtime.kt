package com.chrome.platform.runtime

import com.chrome.platform.events.Event
import kotlin.js.Json

/** https://developer.chrome.com/apps/runtime */
external interface Runtime {
  /** https://developer.chrome.com/apps/runtime#event-onInstalled */
  val onInstalled: Event<(details: Json) -> Unit>
  /** https://developer.chrome.com/extensions/runtime#property-lastError */
  val lastError: Json?
}
