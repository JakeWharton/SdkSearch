package com.jakewharton.sdksearch.store

data class Item(
  val id: Long,
  val listing: String,
  val packageName: String,
  val className: String,
  val deprecated: Boolean,
  val link: String
) {
  companion object {
    private val PACKAGE = "^([a-z0-9]+.)+".toRegex()

    fun createForInsert(listing: String, fqcn: String, link: String, deprecated: Boolean): Item {
      val range = PACKAGE.find(fqcn)?.range
      if (range == null) {
        throw IllegalArgumentException("FQCN '$fqcn' doesn't appear to be valid.")
      }
      val packageName = fqcn.substring(range.start, range.endInclusive)
      val className = fqcn.substring(range.endInclusive + 1)
      return Item(-1, listing, packageName, className, deprecated, link)
    }
  }
}
