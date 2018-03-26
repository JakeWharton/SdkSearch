package com.jakewharton.sdksearch.options

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
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
import kotlin.browser.document
import kotlin.browser.window

val itemCount = document.getElementById("item-count") as HTMLSpanElement

val gitWebInput = document.getElementById("git-web") as HTMLInputElement
val dacInput = document.getElementById("dac") as HTMLInputElement

val resetButton = document.getElementById("reset") as HTMLButtonElement
val saveButton = document.getElementById("save") as HTMLButtonElement

fun main(vararg args: String) {
  val itemStore = StorageAreaItemStore(Chrome.storage.local)
  val configStore = StorageAreaConfigStore(Chrome.storage.sync, PRODUCTION_GIT_WEB, PRODUCTION_DAC)

  // TODO real-time validation of form values.

  document.addEventListener("DOMContentLoaded", {
    loadConfig(itemStore, configStore)
  })

  saveButton.onclick = {
    saveConfig(configStore)
  }

  resetButton.onclick = {
    gitWebInput.value = PRODUCTION_GIT_WEB
    dacInput.value = PRODUCTION_DAC

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

  setFormEnabled(false)

  val config = Config(gitWebUrl, dacUrl)
  launch {
    configStore.save(config)

    saveButton.textContent = "Saved!"
    delay(1000)

    saveButton.textContent = "Save"
    setFormEnabled(true)
  }
}

private fun setFormEnabled(enabled: Boolean) {
  gitWebInput.disabled = !enabled
  dacInput.disabled = !enabled
  saveButton.disabled = !enabled
  resetButton.disabled = !enabled
}
