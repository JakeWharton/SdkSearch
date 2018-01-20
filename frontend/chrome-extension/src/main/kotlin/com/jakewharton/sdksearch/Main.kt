package com.jakewharton.sdksearch

import com.chrome.platform.Chrome.omnibox
import com.chrome.platform.Chrome.storage
import com.chrome.platform.Chrome.tabs
import com.chrome.platform.omnibox.DefaultSuggestResult
import com.chrome.platform.tabs.UpdateProperties
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.FetchDocumentationService
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.store.StorageAreaItemStore
import kotlinx.coroutines.experimental.launch

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

  val service = FetchDocumentationService(BaseUrl(PRODUCTION_DAC))
  val store = StorageAreaItemStore("items", storage.local)

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
