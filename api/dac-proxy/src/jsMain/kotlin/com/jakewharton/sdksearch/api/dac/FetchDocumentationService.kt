package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.w3c.dom.url.URL
import kotlin.browser.window

object FetchDocumentationService : DocumentationService {
  private val listUrl = URL("list", PRODUCTION_PROXY).href

  override fun list(): Deferred<List<DocumentedType>> = window
      .fetch(listUrl)
      .then {
        if (it.status != 200.toShort()) {
          throw RuntimeException("HTTP ${it.status} ${it.statusText}")
        } else {
          it.text()
        }
      }.then {
        Json.parse(DocumentedType.serializer().list, it)
      }
      .asDeferred()
}
