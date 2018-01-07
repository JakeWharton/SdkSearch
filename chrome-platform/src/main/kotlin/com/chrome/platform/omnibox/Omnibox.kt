package com.chrome.platform.omnibox

import com.chrome.platform.events.Event

/** https://developer.chrome.com/extensions/omnibox */
external interface Omnibox {
  /** https://developer.chrome.com/extensions/omnibox#method-setDefaultSuggestion */
  fun setDefaultSuggestion(suggestion: DefaultSuggestResult)

  /** https://developer.chrome.com/extensions/omnibox#event-onInputChanged */
  val onInputChanged: Event<(String, (Array<SuggestResult>) -> Unit) -> Unit>
  /** https://developer.chrome.com/extensions/omnibox#event-onInputEntered */
  val onInputEntered: Event<(String, String) -> Unit>
}
