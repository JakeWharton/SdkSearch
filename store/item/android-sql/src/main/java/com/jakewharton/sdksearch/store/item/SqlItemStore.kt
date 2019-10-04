package com.jakewharton.sdksearch.store.item

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
internal class SqlItemStore @Inject constructor(
  private val db: ItemQueries,
  private val context: CoroutineContext
) : ItemStore {
  override suspend fun updateItems(items: List<Item>) {
    db.transaction {
      val ids = db.findAllItemIds().executeAsList().toMutableSet()

      for (item in items) {
        db.updateItem(item.packageName, item.className, item.deprecated, item.link)
        if (db.changes().executeAsOne() != 0L) {
          ids -= db.findItemId(item.packageName, item.className).executeAsOne()
        } else {
          db.insertItem(item.packageName, item.className, item.deprecated, item.link)
        }
      }

      // SQLite only supports 999 bind args in a single statement.
      ids.chunked(999).forEach { chunk ->
        db.deleteItemIds(chunk)
      }
    }
  }

  override fun queryItems(term: String) =
      db.queryTerm(term.escapeLike('\\')).asFlow().mapToList(context)

  private fun String.escapeLike(escapeChar: Char) =
      this.replace("$escapeChar", "$escapeChar$escapeChar")
          .replace("%", "$escapeChar%")
          .replace("_", "${escapeChar}_")

  override fun count() = db.count().asFlow().mapToOne(context)
}
