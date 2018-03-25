package com.jakewharton.sdksearch.reference

const val PRODUCTION_DAC = "https://developer.android.com/"
const val PRODUCTION_GIT_WEB = "https://android.googlesource.com/"

val ITEM_LIST_URL_PATHS = mapOf(
    "platform" to "reference/lists.js",
    "support" to "reference/android/support/lists.js",
    "wear" to "reference/android/support/wearable/lists.js",
    "test" to "reference/android/support/test/lists.js",
    "constraint" to "reference/android/support/constraint/lists.js",
    "arch" to "reference/android/arch/lists.js"
)

private val PACKAGE = "^([a-z0-9]+.)+".toRegex()

class AndroidReference(
    private val gitWebUrl: String,
    private val dacUrl: String
) {
  init {
    require(gitWebUrl.endsWith('/')) { "Git web URL must end with '/': $gitWebUrl" }
    require(dacUrl.endsWith('/')) { "DAC URL must end with '/': $dacUrl" }
  }

  private val referenceUrl = "${dacUrl}reference/"

  fun sourceUrl(dacUrl: String): String? {
    if (!dacUrl.startsWith(referenceUrl)) return null
    if (!dacUrl.endsWith(".html")) return null
    val fqcn = dacUrl.substring(referenceUrl.length, dacUrl.length - 5).replace('/', '.')
    val range = PACKAGE.find(fqcn)?.range ?: return null
    val packageName = fqcn.substring(range.start, range.endInclusive)
    val className = fqcn.substring(range.endInclusive + 1)
    return sourceUrl(packageName, className)
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
