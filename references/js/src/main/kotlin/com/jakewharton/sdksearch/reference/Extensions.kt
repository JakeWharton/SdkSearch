package com.jakewharton.sdksearch.reference

import org.w3c.dom.url.URL

private val PACKAGE = "^([a-z0-9]+.)+".toRegex()

fun AndroidReference.sourceUrl(dacUrl: String): String? {
  val referenceUrl = "${this.dacUrl}reference/"
  if (!dacUrl.startsWith(referenceUrl)) return null

  val justUrl = URL(dacUrl).pathname.removePrefix("/reference/")
  val fqcn = justUrl.replace('/', '.')
  val range = PACKAGE.find(fqcn)?.range ?: return null

  val packageName = fqcn.substring(range.start, range.endInclusive)
  val className = fqcn.substring(range.endInclusive + 1)
  return sourceUrl(packageName, className)
}
