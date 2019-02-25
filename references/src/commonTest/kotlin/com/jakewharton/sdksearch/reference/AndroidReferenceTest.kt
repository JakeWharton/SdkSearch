package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class AndroidReferenceTest {
  @Test fun sourceUrlForPackageAndClass() {
    val reference = AndroidReference("http://source.example.com/")

    assertNull(reference.sourceUrl("missing.entry", "NotThere"))
    assertNull(reference.sourceUrl("android", "R"))

    assertEquals(
        "http://source.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("android.widget", "Toolbar"))
  }

  @Test fun sourceUrlForNestedClass() {
    val reference = AndroidReference("http://source.example.com/")

    assertEquals(
        "http://source.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("android.widget", "Toolbar.Nested"))
    assertEquals(
        "http://source.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("android.widget", "Toolbar.Doubly.Nested"))
  }

  @Test fun mustEndInSlash() {
    try {
      AndroidReference("http://source.example.com")
      fail()
    } catch (e: IllegalArgumentException) {
      assertEquals("Git web URL must end with '/': http://source.example.com", e.message)
    }
  }
}
