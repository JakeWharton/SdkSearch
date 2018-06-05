package com.jakewharton.sdksearch.options

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter.Event
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.Config
import com.jakewharton.sdksearch.store.StorageAreaConfigStore
import com.jakewharton.sdksearch.store.StorageAreaItemStore
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document

val itemCount = document.getElementById("item-count") as HTMLSpanElement

val gitWebInput = document.getElementById("git-web") as HTMLInputElement
val dacInput = document.getElementById("dac") as HTMLInputElement

val defaultsButton = document.getElementById("defaults") as HTMLButtonElement
val saveButton = document.getElementById("save") as HTMLButtonElement

fun main(vararg args: String) {
  val itemStore = StorageAreaItemStore(Chrome.storage.local)
  val configStore = StorageAreaConfigStore(Chrome.storage.sync, PRODUCTION_GIT_WEB, PRODUCTION_DAC)
  val presenter = OptionsPresenter(DefaultDispatcher, itemStore, configStore)
  presenter.start()

  // TODO validation of form values.

  document.addEventListener("DOMContentLoaded", {
    launch(DefaultDispatcher, UNDISPATCHED) {
      presenter.models.consumeEach {
        console.log(it)

        itemCount.textContent = it.itemCount.toString()

        val config = it.config
        if (config != null) {
          gitWebInput.value = config.gitWebUrl
          dacInput.value = config.dacUrl
        }

        val disableControls = config == null || it.disableUpdates
        gitWebInput.disabled = disableControls
        dacInput.disabled = disableControls
        saveButton.disabled = disableControls
        defaultsButton.disabled = disableControls
      }
    }
  })

  saveButton.onclick = {
    val config = Config(gitWebInput.value, dacInput.value)
    presenter.events.offer(Event.Save(config))
  }

  defaultsButton.onclick = {
    presenter.events.offer(Event.RestoreDefaults)
  }
}
