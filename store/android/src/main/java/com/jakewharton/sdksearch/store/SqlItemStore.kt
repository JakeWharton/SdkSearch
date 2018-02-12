package com.jakewharton.sdksearch.store

import com.squareup.sqlbrite3.BriteDatabase
import com.squareup.sqlbrite3.inTransaction
import com.squareup.sqldelight.SqlDelightQuery
import com.squareup.sqldelight.SqlDelightStatement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SqlItemStore @Inject constructor(
    private val db: BriteDatabase
) : ItemStore {
  private val insertItem by lazy { ItemModel.InsertItem(db.writableDatabase) }
  private val updateItem by lazy { ItemModel.UpdateItem(db.writableDatabase) }
  private val queryTermMapper = SqlItem.FACTORY.queryTermMapper()

  override suspend fun updateItems(items: List<Item>) {
    db.inTransaction {
      for (item in items) {
        val affected = updateItem.update {
          bind(item.packageName, item.className, item.deprecated, item.link)
        }
        if (affected == 0) {
          insertItem.insert {
            bind(item.packageName, item.className, item.deprecated, item.link)
          }
        }
      }
    }
  }

  override fun queryItems(term: String) =
      db.createQuery(SqlItem.FACTORY.queryTerm(term.escapeLike('\\')))
          .mapToList { queryTermMapper.map(it).item }

  override fun count() = db.createQuery(SqlItem.FACTORY.count())
      .mapToOne(SqlItem.FACTORY.countMapper()::map)

  private fun <T : SqlDelightStatement> T.insert(binder: T.() -> Unit): Long {
    synchronized(this) {
      binder()
      return db.executeInsert(table, this)
    }
  }

  private fun <T : SqlDelightStatement> T.update(binder: T.() -> Unit): Int {
    synchronized(this) {
      binder()
      return db.executeUpdateDelete(table, this)
    }
  }
}

private fun String.escapeLike(escapeChar: Char) =
    this.replace("$escapeChar", "$escapeChar$escapeChar")
        .replace("%", "$escapeChar%")
        .replace("_", "${escapeChar}_")

private fun BriteDatabase.createQuery(query: SqlDelightQuery)
    = query.let { createQuery(it.tables, it) }
