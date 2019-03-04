package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AndroidReferenceTest {
  @Test fun sourceUrlForPackageAndClass() {
    val reference = AndroidReference

    assertNull(reference.sourceUrl("missing.entry", "NotThere"))
    assertNull(reference.sourceUrl("android", "R"))

    assertEquals(
        "https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("android.widget", "Toolbar"))
  }

  @Test fun sourceUrlForNestedClass() {
    val reference = AndroidReference

    assertEquals(
        "https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("android.widget", "Toolbar.Nested"))
    assertEquals(
        "https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java",
        reference.sourceUrl("android.widget", "Toolbar.Doubly.Nested"))
  }

  @Test fun noSourceUrlForUnknown() {
    assertNull(AndroidReference.sourceUrl("com.example", "Foo"))
  }
}
