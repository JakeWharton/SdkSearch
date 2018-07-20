package com.chrome.platform.management

import kotlin.js.Json

/** https://developer.chrome.com/extensions/management */
external interface Management {
  /** https://developer.chrome.com/extensions/management#method-get */
  fun get(vararg key: String, callback: (Json) -> Unit)
}
