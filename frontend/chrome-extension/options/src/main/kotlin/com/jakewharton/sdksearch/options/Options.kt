package com.jakewharton.sdksearch.options

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.STORAGE_KEY_DAC_URL
import com.jakewharton.sdksearch.STORAGE_KEY_GITWEB_URL
import com.jakewharton.sdksearch.STORE_NAME
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.StorageAreaItemStore
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.js.json

fun main(vararg args: String) {
  document.addEventListener("DOMContentLoaded", ::loadOptions)
}

private fun loadOptions(event: Event) {
  val store = StorageAreaItemStore(STORE_NAME, Chrome.storage.local)
  launch {
    val count = store.count()
    document.getElementById("item-count")!!.textContent = count.toString()
  }

  Chrome.storage.sync.get(json(
      STORAGE_KEY_GITWEB_URL to PRODUCTION_GIT_WEB,
      STORAGE_KEY_DAC_URL to PRODUCTION_DAC
  )) {
    val gitwebUrl = it[STORAGE_KEY_GITWEB_URL] as String
    document.getElementById("gitweb")!!.setAttribute("value", gitwebUrl)

    val dacUrl = it[STORAGE_KEY_DAC_URL] as String
    document.getElementById("dac")!!.setAttribute("value", dacUrl)
  }
}
