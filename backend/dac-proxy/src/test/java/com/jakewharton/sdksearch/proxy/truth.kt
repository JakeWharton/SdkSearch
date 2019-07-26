package com.jakewharton.sdksearch.proxy

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth

inline fun <reified T : Throwable> assertThrows(body: () -> Unit): ThrowableSubject {
  try {
    body()
  } catch (t: Throwable) {
    if (t is T) {
      return Truth.assertThat(t)
    }
    throw t
  }
  throw AssertionError(
      "Expected ${T::class.java.simpleName} to be thrown by body but it completed successfully")
}
