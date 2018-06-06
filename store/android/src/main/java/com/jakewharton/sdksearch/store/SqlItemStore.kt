package com.jakewharton.sdksearch.store

import com.squareup.sqldelight.runtime.coroutines.asChannel
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext
import kotlinx.coroutines.experimental.channels.ReceiveChannel

@Singleton
internal class SqlItemStore @Inject constructor(
  private val db: ItemQueries,
  private val context: CoroutineContext
) : ItemStore {
  override suspend fun updateItems(items: List<Item>) {
    db.transaction {
      for (item in items) {
        db.insertItem(item.packageName, item.className, item.deprecated, item.link)
        db.insertItemIndex(item.className.asTerms().joinToString(" "))
      }
    }
  }

  override fun queryItems(term: String): ReceiveChannel<List<Item>> {
    val terms = term.asTerms().joinToString(" ") { "$it*" }
    return db.queryTerm("\"$terms\"")
        .asChannel(context)
        .mapToList()
  }

  override fun count() = db.count().asChannel(context).mapToOne()
}
