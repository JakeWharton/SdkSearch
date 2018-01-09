package com.jakewharton.sdksearch.api.dac

import okhttp3.HttpUrl

actual data class BaseUrl actual constructor(actual val url: String) {
  private val httpUrl = HttpUrl.parse(url)!!
  actual fun resolve(link: String) = httpUrl.resolve(link)!!.toString()
}
