package com.jakewharton.sdksearch.api.dac

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.Okio
import okio.Source

internal class JavascriptSkippingInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())
    val realBody = response.body()!!
    val realSource = realBody.source()
    val skipSource = Okio.buffer(
        SkippingSource(realSource))
    val skipBody = ResponseBody.create(realBody.contentType(), -1, skipSource)
    return response.newBuilder().body(skipBody).build()
  }

  private class SkippingSource(private val source: BufferedSource) : Source {
    private var firstRead = true

    override fun read(sink: Buffer, byteCount: Long): Long {
      if (firstRead) {
        // Data starts with a "val SOMETHING =" prefix which we skip on first read.
        source.skip(source.indexOf('='.toByte()) + 1L)
        firstRead = false
      }
      // Request 2 extra bytes. The data ends with a ";<newline>" suffix and we want to pretend like
      // it doesn't exist. If we successfully receive the 2 extra bytes then we can delegate the
      // normal byteCount because the consumer won't see the 2 extra.
      if (source.request(byteCount + 2L)) {
        return source.read(sink, byteCount)
      }

      // We got less than byteCount + 2 bytes which means the ";<newline>" suffix is present.
      val size = source.buffer().size()
      if (size == 2L) {
        // All that's left is the suffix, exhaust it and return EOF.
        source.skip(2L)
        return -1L
      }

      return source.read(sink, size - 2L)
    }

    override fun timeout() = source.timeout()
    override fun close() = source.close()
  }
}
