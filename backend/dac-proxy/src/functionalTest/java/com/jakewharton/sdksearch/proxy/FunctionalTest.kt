package com.jakewharton.sdksearch.proxy

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FunctionalTest {
  @Test fun run() = runBlocking {
    val documentedTypes = listDocumentedTypes()
    val actual = documentedTypes.single { it.className == "View" }
    val expected = DocumentedType("android.view", "View", 1, false, "https://developer.android.com/reference/android/view/View.html")
    assertThat(actual).isEqualTo(expected)
  }
}
