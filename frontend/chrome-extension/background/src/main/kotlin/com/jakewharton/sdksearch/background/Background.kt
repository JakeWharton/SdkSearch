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
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event.QueryChanged
import com.jakewharton.sdksearch.store.config.StorageAreaConfigStore
import com.jakewharton.sdksearch.store.item.StorageAreaItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.launch
import timber.log.ConsoleTree
import timber.log.Timber

fun main(vararg args: String) {
  Timber.plant(ConsoleTree())

  val itemStore = StorageAreaItemStore(storage.local)
  val configStore = StorageAreaConfigStore(storage.sync, PRODUCTION_GIT_WEB, PRODUCTION_DAC)

  launch {
    val config = configStore.load()

    omnibox.setDefaultSuggestion(
        DefaultSuggestResult("Search Android SDK docs for <match>%s</match>"))

    val baseUrl = BaseUrl(config.dacUrl)
    val service = FetchDocumentationService(baseUrl)
    val itemSynchronizer = ItemSynchronizer(itemStore, service)
    val presenter = SearchPresenter(DefaultDispatcher, itemStore, itemSynchronizer)
    val events = presenter.events
    val models = presenter.models

    var currentSuggestions: ((Array<SuggestResult>) -> Unit)? = null
    omnibox.onInputChanged.addListener { text, suggestions ->
      events.offer(QueryChanged(text))
      currentSuggestions = suggestions
    }

    launch {
      for (model in models) {
        val (query, items) = model.queryResults

        val results = items.take(5)
            .map {
              val matchStart = it.className.indexOf(query, ignoreCase = true)
              val matchEnd = matchStart + query.length
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

        currentSuggestions?.invoke(results)
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

    itemSynchronizer.forceSync()
  }
}
