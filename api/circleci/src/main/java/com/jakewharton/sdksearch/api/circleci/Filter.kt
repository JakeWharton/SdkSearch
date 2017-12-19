package com.jakewharton.sdksearch.api.circleci

import java.util.Locale.US

enum class Filter {
  SUCCESSFUL, FAILED, COMPLETED;

  override fun toString(): String {
    return name.toLowerCase(US)
  }
}
