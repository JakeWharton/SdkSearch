package com.jakewharton.sdksearch.store

import androidx.test.InstrumentationRegistry
import com.jakewharton.sdksearch.store.item.DbComponent
import com.jakewharton.sdksearch.store.item.Item.Impl
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
        Impl(-1, "com.example", "One", false, "one.html")
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
        Impl(-1, "com.example", "One", false, "one.html")
    ))

    itemStore.queryItems("One").test {
      val item1 = expectItem().single()
      val id = item1.id
      assertEquals("com.example", item1.packageName)
      assertEquals("One", item1.className)
      assertEquals("one.html", item1.link)
      assertFalse(item1.deprecated)

      itemStore.updateItems(listOf(
          Impl(-1, "com.example", "One", true, "two.html")
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
        Impl(-1, "com.example", "Example1", false, "one.html"),
        Impl(-1, "com.example", "Example2", false, "two.html")
    ))

    itemStore.queryItems("Example").test {
      assertEquals(listOf("Example1", "Example2"), expectItem().map { it.className })

      itemStore.updateItems(listOf(
          Impl(-1, "com.example", "Example1", false, "uno.html"),
          Impl(-1, "com.example", "Example3", false, "tres.html")
      ))
      assertEquals(listOf("Example1", "Example3"), expectItem().map { it.className })

      cancel()
    }
  }

  @Test fun oldItemsDeletedFirstInsert() = runBlocking<Unit> {
    itemStore.count().test {
      assertEquals(0, expectItem())

      itemStore.updateItems(MutableList(2000) {
        Impl(-1, "com.example", "Example$it", false, "item.html")
      })

      assertEquals(2000, expectItem())

      itemStore.updateItems(listOf(
        Impl(-1, "com.example", "Example0", false, "item.html")
      ))

      assertEquals(1, expectItem())

      cancel()
    }
  }

  @Test fun count() = runBlocking<Unit> {
    itemStore.count().test {
      assertEquals(0, expectItem())

      itemStore.updateItems(listOf(
          Impl(-1, "com.example", "One", false, "one.html")
      ))

      assertEquals(1, expectItem())

      itemStore.updateItems(listOf(
          Impl(-1, "com.example", "One", false, "one.html"),
          Impl(-1, "com.example", "Two", false, "two.html")
      ))

      assertEquals(2, expectItem())
      cancel()
    }
  }

  @Test fun wildcards() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        Impl(-1, "com.example", "One%Two", false, "percent.html"),
        Impl(-1, "com.example", "One_Two", false, "underscore.html"),
        Impl(-1, "com.example", "One\\Two", false, "escape.html")
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
