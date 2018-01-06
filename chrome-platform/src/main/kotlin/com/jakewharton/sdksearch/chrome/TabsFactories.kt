package com.jakewharton.sdksearch.chrome

import com.jakewharton.sdksearch.chrome.tabs.UpdateProperties

// TODO remove all of these once https://youtrack.jetbrains.com/issue/KT-21653 is implemented.

fun UpdateProperties(url: String? = null): UpdateProperties {
  val o = js("{}")
  o.url = url
  return o
}
