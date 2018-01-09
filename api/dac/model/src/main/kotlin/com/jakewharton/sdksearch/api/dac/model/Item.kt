package com.jakewharton.sdksearch.api.dac.model

import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class Item(
  val id: Int,
  val label: String,
  val link: String,
  val type: String,
  @Serializable(with = BooleanStringSerializer::class) val deprecated: Boolean
)

@Serializer(forClass = Boolean::class)
object BooleanStringSerializer : KSerializer<Boolean> {
  override fun save(output: KOutput, obj: Boolean) = output.writeStringValue(obj.toString())
  // TODO https://youtrack.jetbrains.com/issue/KT-16348
  override fun load(input: KInput) = input.readStringValue().toLowerCase() == "true"
}
