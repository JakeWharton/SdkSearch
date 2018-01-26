package com.chrome.platform.runtime

external interface Runtime {
  /** https://developer.chrome.com/extensions/runtime#property-lastError */
  var lastError: Throwable?
}
