package com.jakewharton.sdksearch.reference

internal data class SourceLocation(
  val project: SourceProject,
  val branch: String = "master",
  val baseDir: String
) {
  init {
    require(baseDir.endsWith('/'))
  }

  fun branch(branch: String) = copy(branch = branch)
}
