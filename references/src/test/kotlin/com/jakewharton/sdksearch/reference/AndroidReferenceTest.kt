package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class AndroidReferenceTest {
  @Test fun sourceUrlForPackageAndClass() {
    val reference = AndroidReference("http://source.example.com/", "http://docs.example.com/")

    assertNull(reference.sourceUrl("missing.entry", "NotThere"))
    assertNull(reference.sourceUrl("android", "R"))

    assertEquals(
        "http://source.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("android.widget", "Toolbar"))
  }

  @Test fun sourceUrlForDacUrl() {
    val reference = AndroidReference("http://source.example.com/", "http://docs.example.com/")

    assertNull(reference.sourceUrl("http://docs.example.com/reference/missing/entry/NotThere"))
    assertNull(reference.sourceUrl("http://docs.example.com/reference/android/R"))

    assertEquals(
        "http://source.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("http://docs.example.com/reference/android/widget/Toolbar"))
    assertEquals(
        "http://source.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("http://docs.example.com/reference/android/widget/Toolbar.Nested"))
  }

  @Test fun mustEndInSlash() {
    try {
      AndroidReference("http://source.example.com", "http://docs.example.com/")
      fail()
    } catch (e: IllegalArgumentException) {
      assertEquals("Git web URL must end with '/': http://source.example.com", e.message)
    }
    try {
      AndroidReference("http://source.example.com/", "http://docs.example.com")
      fail()
    } catch (e: IllegalArgumentException) {
      assertEquals("DAC URL must end with '/': http://docs.example.com", e.message)
    }
  }
}
