package com.chrome.platform.omnibox

/** https://developer.chrome.com/extensions/omnibox#type-SuggestResult */
external interface SuggestResult : DefaultSuggestResult {
  val content: String
  override val description: String
  val deletable: Boolean?
}

// TODO remove once https://youtrack.jetbrains.com/issue/KT-21653 is implemented.
fun SuggestResult(content: String, description: String, deletable: Boolean? = null): SuggestResult {
  val o = js("{}")
  o.content = content
  o.description = description
  o.deletable = deletable
  return o
}
