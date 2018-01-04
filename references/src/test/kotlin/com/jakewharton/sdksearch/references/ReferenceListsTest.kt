package com.jakewharton.sdksearch.references

import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.Test

class ReferenceListsTest {
  @Test fun sourceUrl() {
    assertNull(sourceUrl("missing.entry", "NotThere"))
    assertNull(sourceUrl("android", "R"))
    assertNotNull(sourceUrl("android.widget", "Toolbar"))
  }
}
