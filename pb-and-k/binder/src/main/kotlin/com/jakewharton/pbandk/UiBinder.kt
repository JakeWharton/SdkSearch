package com.jakewharton.pbandk

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch

interface UiBinder<ModelT : Any> {
  fun bind(model: ModelT, oldModel: ModelT?)
}

fun <ModelT : Any> UiBinder<ModelT>.bindTo(presenter: Presenter<ModelT, *>): Job {
  return launch(Unconfined) {
    var oldModel: ModelT? = null
    for (model in presenter.models) {
      bind(model, oldModel)
      oldModel = model
    }
  }
}
