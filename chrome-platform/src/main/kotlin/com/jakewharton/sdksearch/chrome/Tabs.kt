@file:JsQualifier("chrome")

package com.jakewharton.sdksearch.chrome

/** https://developer.chrome.com/extensions/tabs */
external object tabs {
  /** https://developer.chrome.com/extensions/tabs#method-update */
  fun update(updateProperties: UpdateProperties)

  /** https://developer.chrome.com/extensions/tabs#method-update */
  interface UpdateProperties {
    val url: String?
  }
}
