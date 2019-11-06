package com.jakewharton.sdksearch.proxy

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody

suspend fun Call.awaitBody(): ResponseBody {
  return suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
          it.resume(response.body!!)
        } else {
          it.resumeWithException(IOException("HTTP ${response.code} ${response.message}"))
        }
      }
      override fun onFailure(call: Call, e: IOException) {
        it.resumeWithException(e)
      }
    })
  }
}

// TODO https://github.com/square/okhttp/issues/5322
fun OkHttpClient.Builder.callTimeout(timeout: Duration) = apply {
  callTimeout(timeout.toLongMilliseconds(), TimeUnit.MILLISECONDS)
}
