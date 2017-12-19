package com.jakewharton.sdksearch.api.circleci

import java.util.Locale.US

enum class VcsType {
  GITHUB, BITBUCKET;

  override fun toString(): String {
    return name.toLowerCase(US)
  }
}
