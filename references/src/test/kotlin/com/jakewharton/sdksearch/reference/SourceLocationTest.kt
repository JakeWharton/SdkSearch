package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SourceLocationTest {
  @Test fun branch() {
    val defaultLocation = SourceLocation(SourceProject.BASE, baseDir = "foo/bar/baz/")
    assertEquals("master", defaultLocation.branch)
    val explicitLocation = defaultLocation.branch("other")
    assertEquals("other", explicitLocation.branch)
  }

  @Test fun mustEndInSlash() {
    try {
      SourceLocation(SourceProject.BASE, baseDir = "foo/bar/baz")
      fail()
    } catch (e: IllegalArgumentException) {
      assertEquals("Base dir must end with '/': foo/bar/baz", e.message)
    }
  }
}
