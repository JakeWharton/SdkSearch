package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtensionsTest {
  private val reference = AndroidReference

  @Test fun sourceUrl() {
    val url = reference.sourceUrl("https://developer.android.com/reference/android/widget/Toolbar")
    assertEquals("https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java", url)
  }

  @Test fun sourceUrlNested() {
    val url = reference.sourceUrl("https://developer.android.com/reference/android/widget/Toolbar.Nested")
    assertEquals("https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java", url)
  }

  @Test fun sourceUrlWithQueryAndFragment() {
    val url = reference.sourceUrl("https://developer.android.com/reference/android/widget/Toolbar?ping=pong#whatup")
    assertEquals("https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java", url)
  }

  @Test fun sourceUrlWrongDomain() {
    val url = reference.sourceUrl("https://developer.android2.com/reference/android/view/View")
    assertNull(url)
  }

  @Test fun sourceUrlWrongPath() {
    val url = reference.sourceUrl("https://developer.android.com/reference2/android/view/View")
    assertNull(url)
  }

  @Test fun sourceUrlUnknownType() {
    val url = reference.sourceUrl("https://developer.android.com/reference/missing/entry/NotThere")
    assertNull(url)
  }
}
