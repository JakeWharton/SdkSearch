package com.jakewharton.sdksearch

import com.jakewharton.sdksearch.chrome.DefaultSuggestResult
import com.jakewharton.sdksearch.chrome.UpdateProperties
import com.jakewharton.sdksearch.chrome.omnibox
import com.jakewharton.sdksearch.chrome.tabs
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC

fun main(vararg args: String) {
  omnibox.setDefaultSuggestion(DefaultSuggestResult("Search Android SDK docs for <match>%s</match>"))

  omnibox.onInputChanged.addListener { text, _ ->
    println("OMNIBOX: \"$text\"")

    if (text.isEmpty()) {
      return@addListener
    }

    // TODO all the things!
  }

  omnibox.onInputEntered.addListener { text ->
    val url = if (text.startsWith("http://") || text.startsWith("https://")) {
      text
    } else {
      "${PRODUCTION_DAC}index.html?q=$text"
    }

    tabs.update(UpdateProperties(url = url))
  }
}
