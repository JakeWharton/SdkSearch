package com.jakewharton.presentation

import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

interface UiBinder<ModelT : Any> {
  fun bind(model: ModelT, oldModel: ModelT?)
}

fun <ModelT : Any> UiBinder<ModelT>.bindTo(presenter: Presenter<ModelT, *>): Job {
  return GlobalScope.launch(Dispatchers.Unconfined) {
    var oldModel: ModelT? = null
    for (model in presenter.models) {
      bind(model, oldModel)
      oldModel = model
    }
  }
}
