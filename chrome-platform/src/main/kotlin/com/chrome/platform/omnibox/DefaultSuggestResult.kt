package com.chrome.platform.omnibox

/** https://developer.chrome.com/extensions/omnibox#type-SuggestResult */
external interface DefaultSuggestResult {
  val description: String
}

// TODO remove once https://youtrack.jetbrains.com/issue/KT-21653 is implemented.
fun DefaultSuggestResult(description: String): DefaultSuggestResult {
  val o = js("{}")
  o.description = description
  return o
}
