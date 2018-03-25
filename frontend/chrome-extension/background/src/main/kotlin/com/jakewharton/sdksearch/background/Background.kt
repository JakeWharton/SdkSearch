package com.jakewharton.sdksearch.background

import com.chrome.platform.Chrome.omnibox
import com.chrome.platform.Chrome.storage
import com.chrome.platform.Chrome.tabs
import com.chrome.platform.omnibox.DefaultSuggestResult
import com.chrome.platform.omnibox.SuggestResult
import com.chrome.platform.tabs.UpdateProperties
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.FetchDocumentationService
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.store.StorageAreaItemStore
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

fun main(vararg args: String) {
  omnibox.setDefaultSuggestion(
      DefaultSuggestResult("Search Android SDK docs for <match>%s</match>"))

  val store = StorageAreaItemStore("items", storage.local)
  val baseUrl = BaseUrl(PRODUCTION_DAC)

  var queryJob: Job? = null
  omnibox.onInputChanged.addListener { text, suggestions ->
    queryJob?.cancel()
    if (text.isEmpty()) {
      return@addListener
    }

    queryJob = launch(start = UNDISPATCHED) {
      println("Searching $text")

      val items = store.queryItems(text)
          .take(5)
          .map {
            val matchStart = it.className.indexOf(text, ignoreCase = true)
            val matchEnd = matchStart + text.length
            val description = buildString {
              append("<dim>")
              append(it.packageName)
              append(".</dim>")
              append(it.className.substring(0, matchStart))
              append("<match>")
              append(it.className.substring(matchStart, matchEnd))
              append("</match>")
              append(it.className.substring(matchEnd))
            }
            SuggestResult(baseUrl.resolve(it.link), description, false)
          }.toTypedArray()
      suggestions(items)
    }
  }

  omnibox.onInputEntered.addListener { text, _ ->
    val url = if (text.startsWith("http://") || text.startsWith("https://")) {
      text
    } else {
      "${PRODUCTION_DAC}index.html?q=$text"
    }

    tabs.update(UpdateProperties(url = url))
  }

  val service = FetchDocumentationService(baseUrl)

  ITEM_LIST_URL_PATHS.forEach { (listing, url) ->
    launch {
      val items = service.list(url).await()
          .filter { it.type == "class" }
          .map { Item.createForInsert(it.label, it.link, it.deprecated) }
      store.updateItems(items)

      println("Updated $listing with ${items.size} items")
    }
  }
}
