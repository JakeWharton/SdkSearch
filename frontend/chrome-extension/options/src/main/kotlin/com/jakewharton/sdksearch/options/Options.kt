package com.jakewharton.sdksearch.options

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.store.Config
import com.jakewharton.sdksearch.store.ConfigStore
import com.jakewharton.sdksearch.store.ItemStore
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

val itemCount = document.getElementById("item-count") as HTMLSpanElement

val gitWebInput = document.getElementById("git-web") as HTMLInputElement
val dacInput = document.getElementById("dac") as HTMLInputElement

val resetButton = document.getElementById("reset") as HTMLButtonElement
val saveButton = document.getElementById("save") as HTMLButtonElement

fun main(vararg args: String) {
  val itemStore = StorageAreaItemStore(Chrome.storage.local)
  val configStore = StorageAreaConfigStore(Chrome.storage.sync)

  // TODO real-time validation of form values.

  val loadConfig = { _: Event ->
    loadConfig(itemStore, configStore)
  }
  document.onload = loadConfig
  resetButton.onclick = loadConfig

  saveButton.onclick = {
    saveConfig(configStore)
  }
}

private fun loadConfig(itemStore: ItemStore, configStore: ConfigStore) {
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

private fun saveConfig(configStore: ConfigStore) {
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
