package com.jakewharton.sdksearch.store

import androidx.test.InstrumentationRegistry
import com.jakewharton.sdksearch.store.item.DbComponent
import com.jakewharton.sdksearch.store.item.ItemUtil
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemStoreTest {
  private val itemStore = DbComponent.builder()
      .context(InstrumentationRegistry.getContext())
      .filename(null)
      .coroutineContext(CommonPool)
      .build()
      .itemStore()

  @Test fun query() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "one.html", null)
    ))

    val query = itemStore.queryItems("One")
    val item = query.receive().single()
    assertEquals("com.example", item.packageName)
    assertEquals("One", item.className)
    assertFalse(item.deprecated)
    assertEquals("one.html", item.link)
    query.cancel()
  }

  @Test fun upsert() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "one.html", null)
    ))

    val query = itemStore.queryItems("One")

    val item1 = query.receive().single()
    val id = item1.id
    assertEquals("com.example", item1.packageName)
    assertEquals("One", item1.className)
    assertEquals("one.html", item1.link)
    assertFalse(item1.deprecated)

    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "two.html", "deprecated")
    ))

    val item2 = query.receive().single()
    assertEquals(id, item2.id)
    assertEquals("com.example", item2.packageName)
    assertEquals("One", item2.className)
    assertEquals("two.html", item2.link)
    assertTrue(item2.deprecated)

    query.cancel()
  }

  @Test fun count() = runBlocking<Unit> {
    val query = itemStore.count()

    assertEquals(0, query.receive())

    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "one.html", null)
    ))

    assertEquals(1, query.receive())

    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.Two", "two.html", null),
        ItemUtil.createForInsert("com.example.Three", "three.html", null)
    ))

    assertEquals(3, query.receive())
    query.cancel()
  }

  @Test fun wildcards() = runBlocking<Unit> {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One%Two", "percent.html", null),
        ItemUtil.createForInsert("com.example.One_Two", "underscore.html", null),
        ItemUtil.createForInsert("com.example.One\\Two", "escape.html", null)
    ))

    itemStore.queryItems("%").also {
      assertEquals("One%Two", it.receive().single().className)
      it.cancel()
    }

    itemStore.queryItems("_").also {
      assertEquals("One_Two", it.receive().single().className)
      it.cancel()
    }

    itemStore.queryItems("\\").also {
      assertEquals("One\\Two", it.receive().single().className)
      it.cancel()
    }
  }
}
