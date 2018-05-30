package com.jakewharton.sdksearch.store

import com.squareup.sqldelight.runtime.rx.observe
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SqlItemStore @Inject constructor(
  private val db: ItemQueries,
  private val scheduler: Scheduler
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
      db.queryTerm(term.escapeLike('\\')).observe(scheduler).mapToList()

  private fun String.escapeLike(escapeChar: Char) =
      this.replace("$escapeChar", "$escapeChar$escapeChar")
          .replace("%", "$escapeChar%")
          .replace("_", "${escapeChar}_")

  override fun count() = db.count().observe(scheduler).mapToOne()
}
