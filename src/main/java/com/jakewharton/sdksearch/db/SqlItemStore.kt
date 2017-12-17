package com.jakewharton.sdksearch.db

import com.squareup.sqlbrite3.BriteDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SqlItemStore @Inject constructor(
    private val db: BriteDatabase
) : ItemStore {
  override fun updateListing(listing: String, items: List<Item>) {
    db.newTransaction().use {
      Item.FACTORY.clear_listing(listing).let {
        db.executeAndTrigger(it.tables, it.statement, *it.args)
      }
      for (item in items) {
        db.insert(ItemModel.TABLE_NAME, 0, Item.FACTORY.marshal()
            .listing(listing)
            .label(item.label())
            .link(item.link())
            .type(item.type())
            .asContentValues())
      }
      it.markSuccessful()
    }
  }

  override fun queryItems(term: String) =
      Item.FACTORY.query_term("%$term%").let {
        db.createQuery(it.tables, it.statement, *it.args)
            .mapToList(Item.FACTORY.query_termMapper()::map)
      }

  override fun count() =
      Item.FACTORY.count().let { db.createQuery(it.tables, it.statement) }
          .mapToOne(Item.FACTORY.countMapper()::map)
}
