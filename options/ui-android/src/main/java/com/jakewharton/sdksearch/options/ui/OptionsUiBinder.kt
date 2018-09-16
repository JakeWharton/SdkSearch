package com.jakewharton.sdksearch.options.ui

import android.view.View
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.presentation.UiBinder
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter.Event
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter.Model
import com.jakewharton.sdksearch.store.config.Config
import kotlinx.coroutines.experimental.channels.SendChannel

class OptionsUiBinder(
  view: View,
  private val events: SendChannel<Event>
) : UiBinder<Model> {
  private val gitWebUrlInput: TextInputLayout = view.findViewById(R.id.git_web_url)
  private val dacUrlInput: TextInputLayout = view.findViewById(R.id.dac_url)
  private val saveButton: View = view.findViewById(R.id.save)
  private val restoreDefaultsButton: View = view.findViewById(R.id.restore_defaults)

  init {
    saveButton.setOnClickListener {
      val gitWebUrl = gitWebUrlInput.editText!!.text.toString()
      val dacUrl = dacUrlInput.editText!!.text.toString()
      val config = Config(gitWebUrl, dacUrl)
      events.offer(Event.Save(config))
    }

    restoreDefaultsButton.setOnClickListener {
      events.offer(Event.RestoreDefaults)
    }
  }

  override fun bind(model: Model, oldModel: Model?) {
    val config = model.config
    if (config != null) {
      gitWebUrlInput.editText!!.setText(config.gitWebUrl)
      dacUrlInput.editText!!.setText(config.dacUrl)
    }

    gitWebUrlInput.isEnabled = !model.disableUpdates
    dacUrlInput.isEnabled = !model.disableUpdates
    saveButton.isEnabled = !model.disableUpdates
    restoreDefaultsButton.isEnabled = !model.disableUpdates
  }
}
