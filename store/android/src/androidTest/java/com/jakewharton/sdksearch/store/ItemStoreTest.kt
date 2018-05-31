package com.jakewharton.sdksearch.store

import android.support.test.InstrumentationRegistry
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemStoreTest {
  private val itemStore = DbComponent.builder()
      .context(InstrumentationRegistry.getContext())
      .filename(null)
      .scheduler(Schedulers.trampoline())
      .build()
      .itemStore()

  @Test fun query() = runBlocking {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "one.html", null)
    ))

    itemStore.queryItems("One")
        .test()
        .takeValue {
          val item = it.single()
          assertEquals("com.example", item.packageName)
          assertEquals("One", item.className)
          assertFalse(item.deprecated)
          assertEquals("one.html", item.link)
        }
        .dispose()
  }

  @Test fun upsert() = runBlocking {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "one.html", null)
    ))

    val query = itemStore.queryItems("One").test()

    var id: Long = -1L
    query.takeValue {
      val item = it.single()
      id = item.id
      assertEquals("com.example", item.packageName)
      assertEquals("One", item.className)
      assertEquals("one.html", item.link)
      assertFalse(item.deprecated)
    }

    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One", "two.html", "deprecated")
    ))

    query.takeValue {
      val item = it.single()
      assertEquals(id, item.id)
      assertEquals("com.example", item.packageName)
      assertEquals("One", item.className)
      assertEquals("two.html", item.link)
      assertTrue(item.deprecated)
    }
    query.dispose()
  }

  @Test fun count() = runBlocking {
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

  @Test fun wildcards() = runBlocking {
    itemStore.updateItems(listOf(
        ItemUtil.createForInsert("com.example.One%Two", "percent.html", null),
        ItemUtil.createForInsert("com.example.One_Two", "underscore.html", null),
        ItemUtil.createForInsert("com.example.One\\Two", "escape.html", null)
    ))

    itemStore.queryItems("%")
        .test()
        .takeValue {
          assertEquals("One%Two", it.single().className)
        }
        .dispose()

    itemStore.queryItems("_")
        .test()
        .takeValue {
          assertEquals("One_Two", it.single().className)
        }
        .dispose()

    itemStore.queryItems("\\")
        .test()
        .takeValue {
          assertEquals("One\\Two", it.single().className)
        }
        .dispose()
  }
}

private fun <I> TestObserver<I>.takeValue(handler: (value: I) -> Unit): TestObserver<I> {
  awaitCount(1)
  handler(values().removeAt(0))
  return this
}
