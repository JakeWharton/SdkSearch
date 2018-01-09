package com.jakewharton.retrofit2.converter.kotlinx.serialization

import kotlinx.serialization.KSerialSaver
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter

internal class KSerialSaverConverter<T>(
  private val contentType: MediaType,
  private val saver: KSerialSaver<T>,
  private val serializer: Serializer
) : Converter<T, RequestBody> {
  override fun convert(value: T) = serializer.toRequestBody(contentType, saver, value)
}
