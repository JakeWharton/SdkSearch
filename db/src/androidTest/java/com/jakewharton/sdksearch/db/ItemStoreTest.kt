package com.jakewharton.sdksearch.db

import android.support.test.InstrumentationRegistry
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemStoreTest {
  private val itemStore = DbComponent.builder()
      .context(InstrumentationRegistry.getContext())
      .filename(null)
      .scheduler(Schedulers.trampoline())
      .build()
      .itemStore()

  @Test fun wildcards() {
    itemStore.updateListing("dummy", listOf(
        Item.createForInsert("dummy", "com.example.One%Two", "percent.html", false),
        Item.createForInsert("dummy", "com.example.One_Two", "underscore.html", false),
        Item.createForInsert("dummy", "com.example.One\\Two", "escape.html", false)
    ))

    itemStore.queryItems("%")
        .test()
        .takeValue {
          assertEquals("One%Two", it.single().class_())
        }
        .dispose()

    itemStore.queryItems("_")
        .test()
        .takeValue {
          assertEquals("One_Two", it.single().class_())
        }
        .dispose()

    itemStore.queryItems("\\")
        .test()
        .takeValue {
          assertEquals("One\\Two", it.single().class_())
        }
        .dispose()
  }
}

private fun <I> TestObserver<I>.takeValue(handler: (value: I) -> Unit): TestObserver<I> {
  awaitCount(1)
  handler(values().removeAt(0))
  return this
}
