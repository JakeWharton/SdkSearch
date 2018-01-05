package com.jakewharton.sdksearch

import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS

fun main(vararg args: String) {
  for ((name, path) in ITEM_LIST_URL_PATHS) {
    println("$name $path")
  }
}
