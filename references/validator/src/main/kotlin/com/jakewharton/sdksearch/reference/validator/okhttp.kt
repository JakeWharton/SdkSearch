package com.jakewharton.sdksearch.reference.validator

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Call.await(): Response {
  return suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        it.resume(response)
      }
      override fun onFailure(call: Call, e: IOException) {
        it.resumeWithException(e)
      }
    })
  }
}
