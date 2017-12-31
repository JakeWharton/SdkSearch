package com.jakewharton.sdksearch.db

import com.squareup.sqlbrite3.BriteDatabase
import com.squareup.sqlbrite3.inTransaction
import com.squareup.sqldelight.SqlDelightCompiledStatement
import com.squareup.sqldelight.SqlDelightQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SqlItemStore @Inject constructor(
    private val db: BriteDatabase
) : ItemStore {
  private val clearListing by lazy { ItemModel.Clear_listing(db.writableDatabase) }
  private val insertItem by lazy { ItemModel.Insert_item(db.writableDatabase) }

  override fun updateListing(listing: String, items: List<Item>) {
    db.inTransaction {
      clearListing.insert {
        bind(listing)
      }
      for (item in items) {
        insertItem.insert {
          bind(listing, item.package_(), item.class_(),item.deprecated(), item.link())
        }
      }
    }
  }

  override fun queryItems(term: String) = db.createQuery(Item.FACTORY.query_term(term))
      .mapToList(Item.FACTORY.query_termMapper()::map)

  override fun count() = db.createQuery(Item.FACTORY.count())
      .mapToOne(Item.FACTORY.countMapper()::map)

  private fun <T : SqlDelightCompiledStatement> T.insert(binder: T.() -> Unit) {
    synchronized(this) {
      binder()
      db.executeInsert(table, program)
    }
  }
}

private fun BriteDatabase.createQuery(query: SqlDelightQuery)
    = query.let { createQuery(it.tables, it) }
