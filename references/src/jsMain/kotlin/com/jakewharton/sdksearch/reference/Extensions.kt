package com.jakewharton.sdksearch.reference

import org.w3c.dom.url.URL

private val PACKAGE = "^([a-z0-9]+.)+".toRegex()

fun AndroidReference.sourceUrl(dacUrl: String): String? {
  val url = URL(dacUrl)
  if (url.hostname != "developer.android.com") return null
  if (!url.pathname.startsWith("/reference/")) return null

  val justUrl = url.pathname.removePrefix("/reference/")
  val fqcn = justUrl.replace('/', '.')
  val range = PACKAGE.find(fqcn)?.range ?: return null

  val packageName = fqcn.substring(range.first, range.last)
  val className = fqcn.substring(range.last + 1)
  return sourceUrl(packageName, className)
}
