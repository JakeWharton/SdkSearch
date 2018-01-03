package com.jakewharton.sdksearch.api.dac

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

@JsonQualifier
annotation class BooleanString

object BooleanStringAdapter : JsonAdapter<Boolean>() {
  override fun fromJson(reader: JsonReader) = reader.nextString().toBoolean()
  override fun toJson(writer: JsonWriter, value: Boolean?) {
    writer.value(value.toString())
  }
}
