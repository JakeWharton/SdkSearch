package com.jakewharton.sdksearch

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
import kotlinx.coroutines.experimental.async
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
          .map {
            SuggestResult(baseUrl.resolve(it.link), "${it.packageName}.${it.className}", false)
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
          .map { Item.createForInsert(listing, it.label, it.link, it.deprecated) }
      store.updateListing(listing, items)

      println("Updated $listing with ${items.size} items")
    }
  }
}
