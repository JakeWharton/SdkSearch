package com.jakewharton.sdksearch.db

enum class ItemType {
  PACKAGE,
  CLASS;

  companion object {
    fun parse(name: String) = when (name) {
      "package" -> PACKAGE
      "class" -> CLASS
      else -> throw IllegalArgumentException("Unknown type \"$name\"")
    }
  }
}
