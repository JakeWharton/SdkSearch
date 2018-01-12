package com.jakewharton.sdksearch.api.dac

import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer

@Serializer(forClass = Boolean::class)
internal object BooleanStringSerializer : KSerializer<Boolean> {
  override fun save(output: KOutput, obj: Boolean) = output.writeStringValue(obj.toString())
  override fun load(input: KInput) = input.readStringValue().toBoolean()
}
