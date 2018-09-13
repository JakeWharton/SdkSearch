package com.jakewharton.sdksearch.options

import com.chrome.platform.Chrome
import com.jakewharton.presentation.bindTo
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter
import com.jakewharton.sdksearch.options.ui.OptionsUiBinder
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.config.StorageAreaConfigStore
import kotlinx.coroutines.experimental.Dispatchers
import kotlin.browser.document

fun main(vararg args: String) {
  val configStore = StorageAreaConfigStore(Chrome.storage.sync, PRODUCTION_GIT_WEB, PRODUCTION_DAC)
  val presenter = OptionsPresenter(Dispatchers.Default, configStore)
  presenter.start()

  val binder = OptionsUiBinder(document, presenter.events)

  document.addEventListener("DOMContentLoaded", {
    binder.bindTo(presenter)
  })
}
