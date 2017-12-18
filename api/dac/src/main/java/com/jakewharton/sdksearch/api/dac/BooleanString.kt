package com.jakewharton.sdksearch.api.dac

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

@JsonQualifier
annotation class BooleanString

object BooleanStringAdapter {
  @FromJson @BooleanString fun fromJson(value: String) = value.toBoolean()
  @ToJson fun toJson(@BooleanString value: Boolean) = value.toString()
}
