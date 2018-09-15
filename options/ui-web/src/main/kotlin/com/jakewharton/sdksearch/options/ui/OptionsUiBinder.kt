package com.jakewharton.sdksearch.options.ui

import com.jakewharton.presentation.UiBinder
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter.Event
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter.Model
import com.jakewharton.sdksearch.store.config.Config
import kotlinx.coroutines.experimental.channels.SendChannel
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.NonElementParentNode

class OptionsUiBinder(
  parent: NonElementParentNode,
  private val events: SendChannel<Event>
) : UiBinder<Model>() {
  private val gitWebInput = parent.getElementById("git-web") as HTMLInputElement
  private val dacInput = parent.getElementById("dac") as HTMLInputElement

  private val defaultsButton = parent.getElementById("defaults") as HTMLButtonElement
  private val saveButton = parent.getElementById("save") as HTMLButtonElement

  init {
    saveButton.onclick = {
      val config = Config(gitWebInput.value, dacInput.value)
      events.offer(Event.Save(config))
    }

    defaultsButton.onclick = {
      events.offer(Event.RestoreDefaults)
    }

    // TODO validation of form values.
  }

  override fun bind(model: Model, oldModel: Model?) {
    val config = model.config
    if (config != null) {
      gitWebInput.value = config.gitWebUrl
      dacInput.value = config.dacUrl
    }

    val disableControls = config == null || model.disableUpdates
    gitWebInput.disabled = disableControls
    dacInput.disabled = disableControls
    saveButton.disabled = disableControls
    defaultsButton.disabled = disableControls
  }
}
