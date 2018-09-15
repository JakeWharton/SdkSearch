package com.jakewharton.sdksearch.search.ui

import com.chrome.platform.ChromePlatform
import com.chrome.platform.omnibox.DefaultSuggestResult
import com.chrome.platform.omnibox.SuggestResult
import com.chrome.platform.tabs.UpdateProperties
import com.jakewharton.presentation.UiBinder
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event.QueryChanged
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model
import kotlinx.coroutines.experimental.channels.SendChannel
import org.w3c.dom.url.URL

class SearchUiBinder(
  events: SendChannel<Event>,
  chrome: ChromePlatform,
  private val dacUrl: String
) : UiBinder<Model> {

  private var currentSuggestions: (Array<SuggestResult>) -> Unit = {}

  init {
    chrome.omnibox.setDefaultSuggestion(
        DefaultSuggestResult("Search Android SDK docs for <match>%s</match>"))

    chrome.omnibox.onInputEntered.addListener { text, _ ->
      val url = if (text.startsWith("http://") || text.startsWith("https://")) {
        text
      } else {
        URL("/s/results/?q=$text&p=%2F", dacUrl).toString()
      }

      chrome.tabs.update(UpdateProperties(url = url))
    }

    chrome.omnibox.onInputChanged.addListener { text, suggestions ->
      events.offer(QueryChanged(text))
      currentSuggestions = suggestions
    }
  }

  override fun bind(model: Model, oldModel: Model?) {
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
          val result = URL(it.link, dacUrl).href
          SuggestResult(result, description, false)
        }.toTypedArray()

    currentSuggestions(results)
  }
}
