package com.jakewharton.sdksearch.reference.validator

import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

suspend fun Call.await(): Response {
  return suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        it.resume(response) { response.close() }
      }
      override fun onFailure(call: Call, e: IOException) {
        it.resumeWithException(e)
      }
    })
  }
}
