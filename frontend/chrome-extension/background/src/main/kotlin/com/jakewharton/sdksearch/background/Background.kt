package com.jakewharton.sdksearch.background

import com.chrome.platform.Chrome.omnibox
import com.chrome.platform.Chrome.storage
import com.chrome.platform.Chrome.tabs
import com.chrome.platform.omnibox.DefaultSuggestResult
import com.chrome.platform.omnibox.SuggestResult
import com.chrome.platform.tabs.UpdateProperties
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.FetchDocumentationService
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.ItemUtil
import com.jakewharton.sdksearch.store.StorageAreaConfigStore
import com.jakewharton.sdksearch.store.StorageAreaItemStore
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

fun main(vararg args: String) {
  val itemStore = StorageAreaItemStore(storage.local)
  val configStore = StorageAreaConfigStore(storage.sync, PRODUCTION_GIT_WEB, PRODUCTION_DAC)

  launch {
    val config = configStore.load()

    omnibox.setDefaultSuggestion(
        DefaultSuggestResult("Search Android SDK docs for <match>%s</match>"))

    val baseUrl = BaseUrl(config.dacUrl)

    var queryJob: Job? = null
    omnibox.onInputChanged.addListener { text, suggestions ->
      queryJob?.cancel()
      if (text.isEmpty()) {
        return@addListener
      }

      queryJob = launch(start = UNDISPATCHED) {
        println("Searching $text")

        val items = itemStore.queryItems(text)
            .receive()
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
        "${config.dacUrl}index.html?q=$text"
      }

      tabs.update(UpdateProperties(url = url))
    }

    val service = FetchDocumentationService(baseUrl)

    launch {
      val items = service.list().await()
          .values.single() // TODO gracefully degrade
          .map { ItemUtil.createForInsert(it.type, it.link, it.metadata) }
      itemStore.updateItems(items)

      println("Updated with ${items.size} items")
    }
  }
}
