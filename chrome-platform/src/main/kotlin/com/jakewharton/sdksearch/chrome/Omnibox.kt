@file:JsQualifier("chrome")

package com.jakewharton.sdksearch.chrome

import com.jakewharton.sdksearch.chrome.events.Event.Event2

/** https://developer.chrome.com/extensions/omnibox */
external object omnibox {
  /** https://developer.chrome.com/extensions/omnibox#method-setDefaultSuggestion */
  fun setDefaultSuggestion(suggestion: DefaultSuggestResult)

  /** https://developer.chrome.com/extensions/omnibox#event-onInputChanged */
  val onInputChanged: Event2<String, (Array<SuggestResult>) -> Unit>
  /** https://developer.chrome.com/extensions/omnibox#event-onInputEntered */
  val onInputEntered: Event2<String, String>

  /** https://developer.chrome.com/extensions/omnibox#type-SuggestResult */
  interface DefaultSuggestResult {
    val description: String
  }

  /** https://developer.chrome.com/extensions/omnibox#type-SuggestResult */
  interface SuggestResult : DefaultSuggestResult {
    val content: String
    override val description: String
    val deletable: Boolean?
  }
}
