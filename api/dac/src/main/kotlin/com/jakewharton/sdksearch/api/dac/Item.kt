package com.jakewharton.sdksearch.api.dac

import kotlinx.serialization.Serializable

@Serializable
data class Item(
  val id: Int,
  val label: String,
  val link: String,
  val type: String,
  @Serializable(with = BooleanStringSerializer::class) val deprecated: Boolean
)
