package com.jakewharton.sdksearch.reference

const val PRODUCTION_DAC = "https://developer.android.com/"
const val PRODUCTION_GIT_WEB = "https://android.googlesource.com/"

class AndroidReference(
  val gitWebUrl: String,
  val dacUrl: String
) {
  init {
    require(gitWebUrl.endsWith('/')) { "Git web URL must end with '/': $gitWebUrl" }
    require(dacUrl.endsWith('/')) { "DAC URL must end with '/': $dacUrl" }
  }

  fun sourceUrl(packageName: String, className: String): String? {
    val path = findSourceLocation("$packageName.$className") ?: return null
    return buildString {
      append(gitWebUrl)
      append(path.project.projectDir)
      append("+/refs/heads/")
      append(path.branch)
      append('/')
      append(path.baseDir)
      append(packageName.replace('.', '/'))
      append('/')
      append(className.substringBeforeLast('.'))
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
