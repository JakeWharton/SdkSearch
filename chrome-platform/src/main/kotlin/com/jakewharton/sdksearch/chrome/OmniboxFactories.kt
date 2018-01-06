package com.jakewharton.sdksearch.chrome

import com.jakewharton.sdksearch.chrome.omnibox.DefaultSuggestResult
import com.jakewharton.sdksearch.chrome.omnibox.SuggestResult

// TODO remove all of these once https://youtrack.jetbrains.com/issue/KT-21653 is implemented.

fun DefaultSuggestResult(description: String) : DefaultSuggestResult {
  val o = js("{}")
  o.description = description
  return o
}

fun SuggestResult(content: String, description: String, deletable: Boolean? = null): SuggestResult {
  val o = js("{}")
  o.content = content
  o.description = description
  o.deletable = deletable
  return o
}
