package com.jakewharton.sdksearch.reference

object AndroidReference {
  fun sourceUrl(packageName: String, className: String): String? {
    val path = findSourceLocation("$packageName.$className") ?: return null
    return buildString {
      append("https://android.googlesource.com/")
      append(path.project.projectDir)
      append("+/refs/heads/")
      append(path.branch)
      append('/')
      append(path.baseDir)
      append(packageName.replace('.', '/'))
      append('/')
      append(className.substringBefore('.'))
      append(".java")
    }
  }

  private fun findSourceLocation(fqcn: String): SourceLocation? {
    if (fqcn.contains(".R.") || fqcn.endsWith(".R")) {
      return null
    }

    var lookup = fqcn
    while (lookup.isNotEmpty()) {
      if (SOURCE_MAP.containsKey(lookup)) {
        return SOURCE_MAP[lookup]
      }
      lookup = lookup.substringBeforeLast('.', missingDelimiterValue = "")
    }

    return null // Not found in map.
  }
}
