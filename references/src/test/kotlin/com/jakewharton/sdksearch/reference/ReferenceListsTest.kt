package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReferenceListsTest {
  @Test fun sourceUrl() {
    assertNull(sourceUrl("missing.entry", "NotThere"))
    assertNull(sourceUrl("android", "R"))
    assertNotNull(sourceUrl("android.widget", "Toolbar"))
  }
}
