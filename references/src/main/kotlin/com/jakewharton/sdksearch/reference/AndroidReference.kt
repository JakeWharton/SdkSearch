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

class AndroidReference(private val gitWebUrl: String) {
  init {
    require(gitWebUrl.endsWith('/')) { "Git web URL must end with '/': $gitWebUrl" }
  }

  fun sourceUrl(packageName: String, className: String): String? {
    val topLevelClassName = className.substringBefore('.')
    if (topLevelClassName == "R") {
      return null
    }

    var lookup = "$packageName.$topLevelClassName"
    while (lookup.isNotEmpty()) {
      if (SOURCE_MAP.containsKey(lookup)) {
        val path = SOURCE_MAP[lookup] ?: return null // Explicitly absent.
        return buildString {
          append(gitWebUrl)
          append(path.project.projectDir)
          append("+/refs/heads/")
          append(path.branch)
          append('/')
          append(path.baseDir)
          append(packageName.replace('.', '/'))
          append('/')
          append(topLevelClassName)
          append(".java")
        }
      }
      lookup = lookup.substringBeforeLast('.', missingDelimiterValue = "")
    }

    return null // Not found in map.
  }
}
