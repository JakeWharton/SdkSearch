package com.jakewharton.sdksearch

import com.jakewharton.sdksearch.chrome.DefaultSuggestResult
import com.jakewharton.sdksearch.chrome.UpdateProperties
import com.jakewharton.sdksearch.chrome.omnibox
import com.jakewharton.sdksearch.chrome.tabs
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import kotlin.browser.window

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


  for ((name, path) in ITEM_LIST_URL_PATHS) {
    val url = PRODUCTION_DAC + path
    println("Fetching $name $url…")
    window.fetch(url).then {
      if (it.status != 200.toShort()) {
        println("Failed")
      } else {
        it.text().then {
          // Data starts with a "val SOMETHING =" prefix which we skip.
          val startIndex = it.indexOf('=') + 1
          // Data ends with a ";<newline>" suffix which we skip.
          val endIndex = it.lastIndexOf(';')

          println(it.substring(startIndex, endIndex))
        }
      }
    }
  }
}
