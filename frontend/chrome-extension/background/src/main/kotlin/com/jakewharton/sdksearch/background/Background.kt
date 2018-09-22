package com.jakewharton.sdksearch.background

import com.chrome.platform.Chrome
import com.chrome.platform.Chrome.storage
import com.jakewharton.presentation.bindTo
import com.jakewharton.sdksearch.api.dac.FetchDocumentationService
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.search.ui.SearchUiBinder
import com.jakewharton.sdksearch.store.config.StorageAreaConfigStore
import com.jakewharton.sdksearch.store.item.StorageAreaItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.ConsoleTree
import timber.log.Timber

fun main(vararg args: String) {
  Timber.plant(ConsoleTree())

  val itemStore = StorageAreaItemStore(storage, "local", storage.local)
  val configStore = StorageAreaConfigStore(storage.sync, PRODUCTION_GIT_WEB, PRODUCTION_DAC)

  GlobalScope.launch {
    val config = configStore.load()

    val service = FetchDocumentationService(config.dacUrl)
    val itemSynchronizer = ItemSynchronizer(itemStore, service)

    val presenter = SearchPresenter(itemStore, itemSynchronizer, 0L)
    launch {
      presenter.start()
    }

    withContext(Dispatchers.Unconfined) {
      SearchUiBinder(presenter.events, Chrome, config.dacUrl).bindTo(presenter)
    }
  }
}
