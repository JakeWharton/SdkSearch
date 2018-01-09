package com.jakewharton.retrofit2.converter.kotlinx.serialization

import kotlinx.serialization.KSerialLoader
import okhttp3.ResponseBody
import retrofit2.Converter

internal class KSerialLoaderConverter<T>(
  private val loader: KSerialLoader<T>,
  private val serializer: Serializer
) : Converter<ResponseBody, T> {
  override fun convert(value: ResponseBody) = serializer.fromResponseBody(loader, value)
}
