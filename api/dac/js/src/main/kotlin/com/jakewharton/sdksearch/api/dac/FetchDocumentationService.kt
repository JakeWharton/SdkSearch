package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.asDeferred
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import kotlin.browser.window

class FetchDocumentationService(
  private val baseUrl: BaseUrl
) : DocumentationService {

  override fun list(): Deferred<Map< String, List<Item>>> = window
      .fetch(baseUrl.resolve("_s/getsuggestions?p=%2F&s=irina&c=3"))
      .then {
        if (it.status != 200.toShort()) {
          throw RuntimeException("HTTP ${it.status} ${it.statusText}")
        } else {
          it.text()
        }
      }.then {
        val mapSerializer = (String::class.serializer() to Item.serializer().list).map
        JSON.unquoted.parse(mapSerializer, it)
      }
      .asDeferred()
}
