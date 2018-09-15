package com.jakewharton.sdksearch.ui

import com.jakewharton.sdksearch.api.dac.FetchDocumentationService
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event
import com.jakewharton.sdksearch.store.item.Item
import com.jakewharton.sdksearch.store.item.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.url.URL
import timber.log.ConsoleTree
import timber.log.Timber
import timber.log.debug
import kotlin.browser.document
import kotlin.dom.clear

private const val DAC_PROXY = "https://dac.sdksearch.app"

fun main(vararg args: String) {
  Timber.plant(ConsoleTree())

  val count = document.getElementById("count") as HTMLSpanElement
  val state = document.getElementById("state") as HTMLSpanElement
  val query = document.getElementById("query") as HTMLInputElement
  val items = document.getElementById("items") as HTMLUListElement

  // Because the real DAC lacks CORS headers, we're forced to jump through a proxy.
  val documentationService = FetchDocumentationService(DAC_PROXY)

  val store = InMemoryItemStore()
  val synchronizer = ItemSynchronizer(store, documentationService)

  val presenter = SearchPresenter(store, synchronizer)
  GlobalScope.launch {
    presenter.start()
  }

  GlobalScope.launch(Dispatchers.Unconfined) {
    for (model in presenter.models) {
      Timber.debug { model.toString() }

      count.textContent = model.count.toString()
      state.textContent = model.syncStatus.name

      items.clear()
      model.queryResults.items.forEach { item ->
        val link = document.createElement("a") as HTMLAnchorElement
        link.textContent = "${item.packageName}.${item.className}"
        link.href = URL(item.link, PRODUCTION_DAC).href

        val listItem = document.createElement("li") as HTMLLIElement
        listItem.appendChild(link)

        items.appendChild(listItem)
      }
    }
  }

  query.addEventListener("input", {
    presenter.events.offer(Event.QueryChanged(query.value))
  })
}

class InMemoryItemStore : ItemStore {
  private var items = emptyList<Item>()
  private val countChannel = ConflatedBroadcastChannel(0L)

  override fun count() = countChannel.openSubscription()

  override suspend fun updateItems(items: List<Item>) {
    Timber.debug { "Updating ${items.size} items" }
    this.items = items
    countChannel.offer(items.size.toLong())
  }

  override fun queryItems(term: String): ReceiveChannel<List<Item>> {
    // TODO store in a map and requery when updated?

    val items = items
        .filter { it.className.contains(term, ignoreCase = true) }
        .sortedWith(compareBy {
          val name = it.className
          when {
            name.equals(term, ignoreCase = true) -> 1
            name.startsWith(term, ignoreCase = true) && name.indexOf('.') == -1 -> 2
            name.endsWith(".$term", ignoreCase = true) -> 3
            name.startsWith(term, ignoreCase = true) -> 4
            name.contains(".$term", ignoreCase = true) -> 5
            else -> 6
          }
        })

    return ConflatedBroadcastChannel(items).openSubscription()
  }
}
