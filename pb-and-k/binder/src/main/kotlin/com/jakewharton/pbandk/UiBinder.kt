package com.jakewharton.pbandk

interface UiBinder<ModelT : Any> {
  fun bind(model: ModelT, oldModel: ModelT?)
}
