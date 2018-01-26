package com.chrome.platform

import kotlin.js.Promise

internal inline fun <T> promised(
  crossinline body: (resolve: (T) -> Unit) -> Unit
) = Promise<T> { resolve, reject ->
  body {
    // TODO this breaks inversion of control for ChromePlatform.
    val lastError = Chrome.runtime.lastError
    if (lastError != null) {
      reject(lastError)
    } else {
      resolve(it)
    }
  }
}
