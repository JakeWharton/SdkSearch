package com.jakewharton.sdksearch.db

import org.junit.Assert.assertEquals
import org.junit.Test

class ItemTest {
  @Test fun fqcnParsing() {
    assertEquals(Item(-1, "", "com.example", "Example", false, ""),
        Item.createForInsert("", "com.example.Example", "", false))
    assertEquals(Item(-1, "", "com.example", "Example.Nested", false, ""),
        Item.createForInsert("", "com.example.Example.Nested", "", false))
    assertEquals(Item(-1, "", "com.example", "R.attr", false, ""),
        Item.createForInsert("", "com.example.R.attr", "", false))
  }
}
