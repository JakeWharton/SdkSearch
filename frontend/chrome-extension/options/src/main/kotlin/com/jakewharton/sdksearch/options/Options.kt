package com.jakewharton.sdksearch.options

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.store.Config
import com.jakewharton.sdksearch.store.StorageAreaConfigStore
import com.jakewharton.sdksearch.store.StorageAreaItemStore
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

private val itemStore = StorageAreaItemStore(Chrome.storage.local)
private val configStore = StorageAreaConfigStore(Chrome.storage.sync)

val itemCount = document.getElementById("item-count") as HTMLSpanElement

val gitWebInput = document.getElementById("git-web") as HTMLInputElement
val dacInput = document.getElementById("dac") as HTMLInputElement

val resetButton = document.getElementById("reset") as HTMLButtonElement
val saveButton = document.getElementById("save") as HTMLButtonElement

fun main(vararg args: String) {
  document.addEventListener("DOMContentLoaded", ::loadConfig)

  // TODO real-time validation of form values.

  resetButton.addEventListener("click", ::loadConfig)
  saveButton.addEventListener("click", ::saveConfig)
}

private fun loadConfig(event: Event) {
  launch {
    val count = itemStore.count()
    itemCount.textContent = count.toString()
  }

  launch {
    val (gitWebUrl, dacUrl) = configStore.load()
    gitWebInput.value = gitWebUrl
    dacInput.value = dacUrl
  }
}

private fun saveConfig(event: Event) {
  val gitWebUrl = gitWebInput.value
  if (!gitWebUrl.endsWith('/')) {
    window.alert("Source URL must end with '/': $gitWebUrl")
    return
  }

  val dacUrl = dacInput.value
  if (!dacUrl.endsWith('/')) {
    window.alert("Documentation URL must end with '/': $dacUrl")
    return
  }

  saveButton.disabled = true
  resetButton.disabled = true

  val config = Config(gitWebUrl, dacUrl)
  launch {
    configStore.save(config)

    saveButton.textContent = "Saved!"
    delay(1000)

    saveButton.textContent = "Save"
    saveButton.disabled = false
    resetButton.disabled = false
  }
}
