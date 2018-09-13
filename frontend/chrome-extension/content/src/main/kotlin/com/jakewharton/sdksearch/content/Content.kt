package com.jakewharton.sdksearch.content

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.reference.sourceUrl
import com.jakewharton.sdksearch.store.config.StorageAreaConfigStore
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import timber.log.ConsoleTree
import timber.log.Timber
import timber.log.debug
import timber.log.warn
import kotlin.browser.document
import kotlin.browser.window

fun main(vararg args: String) {
  Timber.plant(ConsoleTree())

  val configStore = StorageAreaConfigStore(Chrome.storage.sync, PRODUCTION_GIT_WEB, PRODUCTION_DAC)

  GlobalScope.launch {
    val (gitWebUrl, dacUrl) = configStore.load()
    val references = AndroidReference(gitWebUrl, dacUrl)

    val sourceUrl = references.sourceUrl(window.location.href)
    if (sourceUrl == null) {
      Timber.debug { "No source URL for ${window.location.href}" }
      return@launch
    }

    val targetElement = document.querySelector("#api-info-block .api-level")
    if (targetElement != null) {
      val br = document.createElement("br")
      targetElement.appendChild(br)

      val link = document.createElement("a")
      link.setAttribute("href", sourceUrl)
      link.textContent = "view source"
      targetElement.appendChild(link)
    } else {
      Timber.warn { "Could not find on-page element to add 'view source' link" }
    }
  }
}
