package com.chrome.platform.runtime

import kotlin.js.Json

/** https://developer.chrome.com/extensions/runtime */
external interface Runtime {
  fun getManifest(): Json
}
