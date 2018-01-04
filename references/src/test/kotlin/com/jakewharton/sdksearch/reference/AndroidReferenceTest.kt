package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.fail

class AndroidReferenceTest {
  @Test fun sourceUrl() {
    val reference = AndroidReference("http://example.com/")
    assertNull(reference.sourceUrl("missing.entry", "NotThere"))
    assertNull(reference.sourceUrl("android", "R"))
    assertNotNull(reference.sourceUrl("android.widget", "Toolbar"))
  }

  @Test fun mustEndInSlash() {
    try {
      AndroidReference("http://example.com")
      fail()
    } catch (e: IllegalArgumentException) {
      assertEquals("Git web URL must end with '/': http://example.com", e.message)
    }
  }
}
