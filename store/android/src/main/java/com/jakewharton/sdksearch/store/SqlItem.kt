package com.jakewharton.sdksearch.store

import com.jakewharton.sdksearch.store.ItemModel.Factory

// TODO https://github.com/square/sqldelight/issues/227
internal class SqlItem(val item: Item) : ItemModel {
  override fun id() = item.id
  override fun listing() = item.listing
  override fun packageName() = item.packageName
  override fun className() = item.className
  override fun deprecated() = item.deprecated
  override fun link() = item.link

  companion object {
    val FACTORY: Factory<SqlItem> = Factory { id, listing, packageName, className, deprecated, link ->
      SqlItem(Item(id, listing, packageName, className, deprecated, link))
    }
  }
}
