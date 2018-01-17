package com.jakewharton.sdksearch.db

import com.jakewharton.sdksearch.db.ItemModel.Factory
import java.util.regex.Pattern
import kotlin.DeprecationLevel.HIDDEN

data class Item(
  val id: Long,
  val listing: String,
  val packageName: String,
  val className: String,
  val deprecated: Boolean,
  val link: String
) : ItemModel {
  // TODO https://github.com/square/sqldelight/issues/227

  @Deprecated("For SQL Delight", level = HIDDEN)
  override fun id() = id
  @Deprecated("For SQL Delight", level = HIDDEN)
  override fun listing() = listing
  @Deprecated("For SQL Delight", level = HIDDEN)
  override fun packageName() = packageName
  @Deprecated("For SQL Delight", level = HIDDEN)
  override fun className() = className
  @Deprecated("For SQL Delight", level = HIDDEN)
  override fun deprecated() = deprecated
  @Deprecated("For SQL Delight", level = HIDDEN)
  override fun link() = link

  companion object {
    val FACTORY: Factory<Item> = Factory(::Item)
    private val PACKAGE = Pattern.compile("^([a-z0-9]+.)+") // TODO Kotlin regex

    fun createForInsert(listing: String, fqcn: String, link: String, deprecated: Boolean): Item {
      val matcher = PACKAGE.matcher(fqcn)
      if (!matcher.lookingAt()) {
        throw IllegalArgumentException("FQCN '$fqcn' doesn't appear to be valid.")
      }
      val packageName = fqcn.substring(0, matcher.end() - 1)
      val className = fqcn.substring(matcher.end())
      return Item(-1, listing, packageName, className, deprecated, link)
    }
  }
}
