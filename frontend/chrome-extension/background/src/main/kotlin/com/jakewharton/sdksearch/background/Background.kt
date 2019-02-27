package com.jakewharton.sdksearch.background

import com.chrome.platform.Chrome
import com.chrome.platform.Chrome.storage
import com.jakewharton.presentation.bindTo
import com.jakewharton.sdksearch.api.dac.FetchDocumentationService
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.search.ui.SearchUiBinder
import com.jakewharton.sdksearch.store.item.StorageAreaItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.ConsoleTree
import timber.log.Timber

fun main() {
  Timber.plant(ConsoleTree())

  val itemStore = StorageAreaItemStore(storage, "local", storage.local)
  val itemSynchronizer = ItemSynchronizer(itemStore, FetchDocumentationService)

  val presenter = SearchPresenter(itemStore, itemSynchronizer, 0L)

  GlobalScope.launch {
    presenter.start()
  }
  GlobalScope.launch(Dispatchers.Unconfined) {
    SearchUiBinder(presenter.events, Chrome).bindTo(presenter)
  }
}
