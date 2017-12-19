package com.jakewharton.sdksearch.api.dac

import okhttp3.HttpUrl

data class BaseUrl(val url: String) {
  private val httpUrl = HttpUrl.parse(url)!!
  fun resolve(link: String) = httpUrl.resolve(link)!!.toString()
}
