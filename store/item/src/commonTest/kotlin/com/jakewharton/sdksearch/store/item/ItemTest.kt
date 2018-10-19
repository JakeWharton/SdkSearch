package com.jakewharton.sdksearch.store.item

import kotlin.test.Test
import kotlin.test.assertEquals

class ItemTest {
  @Test fun fqcnParsing() {
    assertEquals(Item.Impl(-1, "com.example", "Example", false, ""),
        ItemUtil.createForInsert("com.example.Example", "", null))
    assertEquals(Item.Impl(-1, "com.example", "Example.Nested", false, ""),
        ItemUtil.createForInsert("com.example.Example.Nested", "", null))
    assertEquals(Item.Impl(-1, "com.example", "R.attr", false, ""),
        ItemUtil.createForInsert("com.example.R.attr", "", null))
  }
}
