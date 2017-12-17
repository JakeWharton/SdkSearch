package com.jakewharton.sdksearch.api.dac

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Item(
  val id: Long,
  val label: String,
  val link: String,
  val type: String
)
