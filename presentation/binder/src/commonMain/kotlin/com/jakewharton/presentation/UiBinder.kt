package com.jakewharton.presentation

interface UiBinder<ModelT : Any> {
  fun bind(model: ModelT, oldModel: ModelT?)
}

suspend fun <ModelT : Any> UiBinder<ModelT>.bindTo(presenter: Presenter<ModelT, *>) {
  var oldModel: ModelT? = null
  for (model in presenter.models) {
    bind(model, oldModel)
    oldModel = model
  }
}
