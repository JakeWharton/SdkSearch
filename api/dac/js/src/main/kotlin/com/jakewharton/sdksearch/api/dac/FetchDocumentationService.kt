package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.asDeferred
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import org.w3c.dom.url.URL
import kotlin.browser.window

class FetchDocumentationService(
  baseUrl: String
) : DocumentationService {
  private val listUrl = URL("_s/getsuggestions?p=%2F&s=irina&c=3", baseUrl).href

  override fun list(): Deferred<Map< String, List<Item>>> = window
      .fetch(listUrl)
      .then {
        if (it.status != 200.toShort()) {
          throw RuntimeException("HTTP ${it.status} ${it.statusText}")
        } else {
          it.text()
        }
      }.then {
        val mapSerializer = (String.serializer() to Item.serializer().list).map
        JSON.nonstrict.parse(mapSerializer, it)
      }
      .asDeferred()
}
