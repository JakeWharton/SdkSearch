package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.asDeferred
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import kotlin.browser.window

class FetchDocumentationService(
  private val baseUrl: String
) : DocumentationService {

  override fun list(relativeUrl: String): Deferred<List<Item>> = window
      .fetch(baseUrl + relativeUrl)
      .then {
        if (it.status != 200.toShort()) {
          throw RuntimeException("HTTP ${it.status} ${it.statusText}")
        } else {
          it.text()
        }
      }.then {
        // Data starts with a "val SOMETHING =" prefix which we skip.
        val startIndex = it.indexOf('=') + 1
        // Data ends with a ";<newline>" suffix which we skip.
        val endIndex = it.lastIndexOf(';')

        val json = it.substring(startIndex, endIndex)

        JSON.unquoted.parse(Item.serializer().list, json)
      }
      .asDeferred()
}
