package com.jakewharton.sdksearch.api.dac

import com.squareup.moshi.JsonAdapter
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
internal abstract class  ApiJsonAdapterFactory : JsonAdapter.Factory {
  companion object {
    @JvmField val INSTANCE: ApiJsonAdapterFactory = KotshiApiJsonAdapterFactory()
  }
}
