@file:JvmName("KotlinxSerializationMessageBodyWriter")

package com.jakewharton.rs.kotlinx.serialization

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializerByTypeToken
import java.io.OutputStream
import java.lang.reflect.Type
import java.nio.charset.Charset
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyWriter

@JvmName("create")
fun StringFormat.asMessageBodyWriter(vararg mediaTypes: MediaType): MessageBodyWriter<Any> {
  return StringFormatMessageBodyWriter(this, mediaTypes.toSet())
}

@JvmName("create")
fun BinaryFormat.asMessageBodyWriter(vararg mediaTypes: MediaType): MessageBodyWriter<Any> {
  return BinaryFormatMessageBodyWriter(this, mediaTypes.toSet())
}

private class StringFormatMessageBodyWriter(
  private val format: StringFormat,
  private val mediaTypes: Set<MediaType>
) : MessageBodyWriter<Any> {
  override fun isWriteable(
    type: Class<*>,
    genericType: Type,
    annotations: Array<out Annotation>,
    mediaType: MediaType
  ): Boolean {
    return mediaType in mediaTypes
  }

  override fun writeTo(
    value: Any,
    type: Class<*>,
    genericType: Type,
    annotations: Array<out Annotation>,
    mediaType: MediaType,
    httpHeaders: MultivaluedMap<String, Any>,
    entityStream: OutputStream
  ) {
    val string = format.stringify(serializerByTypeToken(genericType), value)
    val charset = try {
      mediaType.parameters["charset"]?.let(Charset::forName)
    } catch (e: RuntimeException) {
      null
    } ?: Charsets.UTF_8

    entityStream.write(string.toByteArray(charset))
  }
}

private class BinaryFormatMessageBodyWriter(
  private val format: BinaryFormat,
  private val mediaTypes: Set<MediaType>
) : MessageBodyWriter<Any> {
  override fun isWriteable(
    type: Class<*>,
    genericType: Type,
    annotations: Array<out Annotation>,
    mediaType: MediaType
  ): Boolean {
    return mediaType in mediaTypes
  }

  override fun writeTo(
    value: Any,
    type: Class<*>,
    genericType: Type,
    annotations: Array<out Annotation>,
    mediaType: MediaType,
    httpHeaders: MultivaluedMap<String, Any>,
    entityStream: OutputStream
  ) {
    val bytes = format.dump(serializerByTypeToken(genericType), value)
    entityStream.write(bytes) // Not closing per parameter Javadoc.
  }
}
