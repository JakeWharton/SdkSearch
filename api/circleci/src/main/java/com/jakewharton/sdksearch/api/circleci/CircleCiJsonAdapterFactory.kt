package com.jakewharton.sdksearch.api.circleci

import com.squareup.moshi.JsonAdapter
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
internal abstract class CircleCiJsonAdapterFactory : JsonAdapter.Factory {
  companion object {
    @JvmField val INSTANCE: CircleCiJsonAdapterFactory = KotshiCircleCiJsonAdapterFactory()
  }
}
