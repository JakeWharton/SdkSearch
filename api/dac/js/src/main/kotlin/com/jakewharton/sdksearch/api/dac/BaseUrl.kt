package com.jakewharton.sdksearch.api.dac

import org.w3c.dom.url.URL

actual data class BaseUrl actual constructor(actual val url: String) {
  actual fun resolve(link: String) = URL(link, url).href
}
