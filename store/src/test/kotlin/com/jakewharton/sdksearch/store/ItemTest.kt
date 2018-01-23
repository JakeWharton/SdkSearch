package com.jakewharton.sdksearch.store

import kotlin.test.Test
import kotlin.test.assertEquals

class ItemTest {
  @Test fun fqcnParsing() {
    assertEquals(Item(-1, "com.example", "Example", false, ""),
        Item.createForInsert("com.example.Example", "", false))
    assertEquals(Item(-1, "com.example", "Example.Nested", false, ""),
        Item.createForInsert("com.example.Example.Nested", "", false))
    assertEquals(Item(-1, "com.example", "R.attr", false, ""),
        Item.createForInsert("com.example.R.attr", "", false))
  }
}
