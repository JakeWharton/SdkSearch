package com.jakewharton.sdksearch.api.dac

expect class BaseUrl(url: String) {
  val url: String
  fun resolve(link: String): String
}
