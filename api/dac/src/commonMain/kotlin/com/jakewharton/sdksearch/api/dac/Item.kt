package com.jakewharton.sdksearch.api.dac

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
  @SerialName("t")
  val type: String,
  @SerialName("p")
  val link: String,
  @SerialName("r")
  val metadata: String?
)
