package com.jakewharton.sdksearch.references

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReferenceListsTest {
  @Test fun sourceUrl() {
    assertNull(sourceUrl("missing.entry", "NotThere"))
    assertNull(sourceUrl("org.json", "JsonObject"))
    assertNotNull(sourceUrl("android.widget", "Toolbar"))
  }
}
