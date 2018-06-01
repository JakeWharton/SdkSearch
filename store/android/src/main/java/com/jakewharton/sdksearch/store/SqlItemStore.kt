package com.jakewharton.sdksearch.store

import com.squareup.sqldelight.runtime.coroutines.asChannel
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Singleton
internal class SqlItemStore @Inject constructor(
  private val db: ItemQueries,
  private val context: CoroutineContext
) : ItemStore {
  override suspend fun updateItems(items: List<Item>) {
    db.transaction {
      for (item in items) {
        val affected = db.updateItem(item.deprecated, item.link, item.packageName, item.className)
        if (affected == 0L) {
          db.insertItem(item.packageName, item.className, item.deprecated, item.link)
        }
      }
    }
  }

  override fun queryItems(term: String) =
      db.queryTerm(term.escapeLike('\\')).asChannel(context).mapToList()

  private fun String.escapeLike(escapeChar: Char) =
      this.replace("$escapeChar", "$escapeChar$escapeChar")
          .replace("%", "$escapeChar%")
          .replace("_", "${escapeChar}_")

  override fun count() = db.count().asChannel(context).mapToOne()
}
