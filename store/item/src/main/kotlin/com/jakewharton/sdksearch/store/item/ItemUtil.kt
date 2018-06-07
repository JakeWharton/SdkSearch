package com.jakewharton.sdksearch.store.item

import com.jakewharton.sdksearch.store.item.Item.Impl
import kotlin.IllegalArgumentException

object ItemUtil {
  private val PACKAGE = "^([a-z0-9]+.)+".toRegex()

  fun createForInsert(fqcn: String, link: String, metadata: String?): Item {
    val range = PACKAGE.find(fqcn)?.range
    if (range == null) {
      throw IllegalArgumentException("FQCN '$fqcn' doesn't appear to be valid.")
    }
    val packageName = fqcn.substring(range.start, range.endInclusive)
    val className = fqcn.substring(range.endInclusive + 1)
    val deprecated = metadata == "deprecated"
    return Impl(-1, packageName, className, deprecated, link)
  }
}
