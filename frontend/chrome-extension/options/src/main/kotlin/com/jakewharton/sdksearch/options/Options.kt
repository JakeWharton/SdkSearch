package com.jakewharton.sdksearch.options

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.store.StorageAreaConfigStore
import com.jakewharton.sdksearch.store.StorageAreaItemStore
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.events.Event
import kotlin.browser.document

private val itemStore = StorageAreaItemStore(Chrome.storage.local)
private val configStore = StorageAreaConfigStore(Chrome.storage.sync)

fun main(vararg args: String) {
  document.addEventListener("DOMContentLoaded", ::loadOptions)
}

private fun loadOptions(event: Event) {
  launch {
    val count = itemStore.count()
    document.getElementById("item-count")!!.textContent = count.toString()
  }

  launch {
    val (gitWebUrl, dacUrl) = configStore.load()
    document.getElementById("git-web")!!.setAttribute("value", gitWebUrl)
    document.getElementById("dac")!!.setAttribute("value", dacUrl)
  }
}
