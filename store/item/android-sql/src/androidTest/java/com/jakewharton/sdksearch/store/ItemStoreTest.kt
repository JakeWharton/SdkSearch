package com.jakewharton.sdksearch.store

import androidx.test.InstrumentationRegistry
import com.jakewharton.sdksearch.store.item.DbComponent
import com.jakewharton.sdksearch.store.item.ItemUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemStoreTest {
  private val itemStore = DbComponent.builder()
      .context(InstrumentationRegistry.getContext())
      .filename(null)
      .coroutineContext(Dispatchers.IO)
      .build()
      .itemStore()

  @Test fun query() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "one.html", null)
    ))

    itemStore.queryItems("One").test {
      val item = expectItem().single()
      assertEquals("com.example", item.packageName)
      assertEquals("One", item.className)
      assertFalse(item.deprecated)
      assertEquals("one.html", item.link)

      cancel()
    }
  }

  @Test fun upsert() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "one.html", null)
    ))

    itemStore.queryItems("One").test {
      val item1 = expectItem().single()
      val id = item1.id
      assertEquals("com.example", item1.packageName)
      assertEquals("One", item1.className)
      assertEquals("one.html", item1.link)
      assertFalse(item1.deprecated)

      itemStore.updateItems(listOf(
          ItemUtil.createForInsert("com.example.One", "two.html", "deprecated")
      ))

      val item2 = expectItem().single()
      assertEquals(id, item2.id)
      assertEquals("com.example", item2.packageName)
      assertEquals("One", item2.className)
      assertEquals("two.html", item2.link)
      assertTrue(item2.deprecated)

      cancel()
    }
  }

  @Test fun oldItemsDeleted() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.Example1", "one.html", null),
        ItemUtil.createForInsert("com.example.Example2", "two.html", null)
    ))

    itemStore.queryItems("Example").test {
      assertEquals(listOf("Example1", "Example2"), expectItem().map { it.className })

      itemStore.updateItems(listOf(
          ItemUtil.createForInsert("com.example.Example1", "uno.html", null),
          ItemUtil.createForInsert("com.example.Example3", "tres.html", null)
      ))
      assertEquals(listOf("Example1", "Example3"), expectItem().map { it.className })

      cancel()
    }
  }

  @Test fun count() = runBlocking<Unit> {
    itemStore.count().test {
      assertEquals(0, expectItem())

      itemStore.updateItems(listOf(
          ItemUtil.createForInsert("com.example.One", "one.html", null)
      ))

      assertEquals(1, expectItem())

      itemStore.updateItems(listOf(
          ItemUtil.createForInsert("com.example.One", "one.html", null),
          ItemUtil.createForInsert("com.example.Two", "two.html", null)
      ))

      assertEquals(2, expectItem())
      cancel()
    }
  }

  @Test fun wildcards() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One%Two", "percent.html", null),
        ItemUtil.createForInsert("com.example.One_Two", "underscore.html", null),
        ItemUtil.createForInsert("com.example.One\\Two", "escape.html", null)
    ))

    itemStore.queryItems("%").test {
      assertEquals("One%Two", expectItem().single().className)
      cancel()
    }

    itemStore.queryItems("_").test {
      assertEquals("One_Two", expectItem().single().className)
      cancel()
    }

    itemStore.queryItems("\\").test {
      assertEquals("One\\Two", expectItem().single().className)
      cancel()
    }
  }
}
