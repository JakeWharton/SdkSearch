package com.jakewharton.sdksearch.content

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.store.StorageAreaConfigStore
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.browser.document
import kotlin.browser.window

fun main(vararg args: String) {
  val configStore = StorageAreaConfigStore(Chrome.storage.sync)

  launch {
    val (gitWebUrl, dacUrl) = configStore.load()
    val references = AndroidReference(gitWebUrl, dacUrl)

    val sourceUrl = references.sourceUrl(window.location.href) ?: return@launch

    val br = document.createElement("br")

    val link = document.createElement("a")
    link.setAttribute("href", sourceUrl)
    link.textContent = "View source"

    val div = document.createElement("div")
    div.appendChild(br)
    div.appendChild(link)

    document.querySelector("#api-info-block")?.appendChild(div)
  }
}
