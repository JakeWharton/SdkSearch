package com.jakewharton.sdksearch

import com.chrome.platform.Chrome.omnibox
import com.chrome.platform.Chrome.storage
import com.chrome.platform.Chrome.tabs
import com.chrome.platform.omnibox.DefaultSuggestResult
import com.chrome.platform.tabs.UpdateProperties
import com.jakewharton.sdksearch.api.dac.FetchDocumentationService
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import kotlinx.coroutines.experimental.launch
import kotlin.js.json

fun main(vararg args: String) {
  omnibox.setDefaultSuggestion(
      DefaultSuggestResult("Search Android SDK docs for <match>%s</match>"))

  omnibox.onInputChanged.addListener { text, _ ->
    println("OMNIBOX: \"$text\"")

    if (text.isEmpty()) {
      return@addListener
    }

    // TODO all the things!
  }

  omnibox.onInputEntered.addListener { text, _ ->
    val url = if (text.startsWith("http://") || text.startsWith("https://")) {
      text
    } else {
      "${PRODUCTION_DAC}index.html?q=$text"
    }

    tabs.update(UpdateProperties(url = url))
  }

  val service = FetchDocumentationService(PRODUCTION_DAC)

  launch {
    val items = ITEM_LIST_URL_PATHS.values
        .map { service.list(it) }
        .flatMap { it.await() }
        .filter { it.type == "class" }
        .sortedBy { it.label }

    storage.local.set(json("items" to items)) {
      println("Stored ${items.size} items")
    }

    println(items)
  }
}
