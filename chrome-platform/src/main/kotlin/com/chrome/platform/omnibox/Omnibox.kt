package com.chrome.platform.omnibox

import com.chrome.platform.events.Event2

/** https://developer.chrome.com/extensions/omnibox */
external interface Omnibox {
  /** https://developer.chrome.com/extensions/omnibox#method-setDefaultSuggestion */
  fun setDefaultSuggestion(suggestion: DefaultSuggestResult)

  /** https://developer.chrome.com/extensions/omnibox#event-onInputChanged */
  val onInputChanged: Event2<String, (Array<SuggestResult>) -> Unit>
  /** https://developer.chrome.com/extensions/omnibox#event-onInputEntered */
  val onInputEntered: Event2<String, String>
}
