package com.chrome.platform.tabs

/** https://developer.chrome.com/extensions/tabs#method-update */
external interface UpdateProperties {
  val url: String?
}

// TODO remove once https://youtrack.jetbrains.com/issue/KT-21653 is implemented.
fun UpdateProperties(url: String? = null): UpdateProperties {
  val o = js("{}")
  o.url = url
  return o
}
