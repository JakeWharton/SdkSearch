package com.jakewharton.presentation

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

abstract class UiBinder<ModelT : Any> : CoroutineScope {
  private val job = Job()
  override val coroutineContext = Dispatchers.Unconfined + job

  fun cancel() {
    job.cancel()
  }

  abstract fun bind(model: ModelT, oldModel: ModelT?)
}

fun <ModelT : Any> UiBinder<ModelT>.bindTo(presenter: Presenter<ModelT, *>): Job {
  return launch {
    var oldModel: ModelT? = null
    for (model in presenter.models) {
      bind(model, oldModel)
      oldModel = model
    }
  }
}
