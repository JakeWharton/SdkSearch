@file:JvmName("KotlinxSerializationMessageBodyReader")

package com.jakewharton.rs.kotlinx.serialization

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializerByTypeToken
import java.io.InputStream
import java.lang.reflect.Type
import java.nio.charset.Charset
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader

@JvmName("create")
fun StringFormat.asMessageBodyReader(vararg mediaTypes: MediaType): MessageBodyReader<Any> {
  return StringFormatMessageBodyReader(this, mediaTypes.toSet())
}

@JvmName("create")
fun BinaryFormat.asMessageBodyReader(vararg mediaTypes: MediaType): MessageBodyReader<Any> {
  return BinaryFormatMessageBodyReader(this, mediaTypes.toSet())
}

private class StringFormatMessageBodyReader(
  private val format: StringFormat,
  private val mediaTypes: Set<MediaType>
) : MessageBodyReader<Any> {
  override fun isReadable(
    type: Class<*>,
    genericType: Type,
    annotations: Array<out Annotation>,
    mediaType: MediaType
  ): Boolean {
    return mediaType in mediaTypes
  }

  override fun readFrom(
    type: Class<Any>,
    genericType: Type,
    annotations: Array<out Annotation>,
    mediaType: MediaType,
    httpHeaders: MultivaluedMap<String, String>,
    entityStream: InputStream
  ): Any {
    val charset = try {
      mediaType.parameters["charset"]?.let(Charset::forName)
    } catch (e: RuntimeException) {
      null
    } ?: Charsets.UTF_8

    val string = entityStream.readBytes().toString(charset) // Not closing per parameter Javadoc.
    return format.parse(serializerByTypeToken(genericType), string)
  }
}

private class BinaryFormatMessageBodyReader(
  private val format: BinaryFormat,
  private val mediaTypes: Set<MediaType>
) : MessageBodyReader<Any> {
  override fun isReadable(
    type: Class<*>?,
    genericType: Type?,
    annotations: Array<out Annotation>?,
    mediaType: MediaType?
  ): Boolean {
    return mediaType in mediaTypes
  }

  override fun readFrom(
    type: Class<Any>,
    genericType: Type,
    annotations: Array<out Annotation>,
    mediaType: MediaType,
    httpHeaders: MultivaluedMap<String, String>,
    entityStream: InputStream
  ): Any {
    val bytes = entityStream.readBytes() // Not closing per parameter Javadoc.
    return format.load(serializerByTypeToken(genericType), bytes)
  }
}
