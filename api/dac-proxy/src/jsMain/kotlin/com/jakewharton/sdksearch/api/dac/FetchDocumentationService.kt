package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import kotlin.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.w3c.dom.url.URL

object FetchDocumentationService : DocumentationService {
  private val listUrl = URL("list", PRODUCTION_PROXY).href

  override suspend fun list(): List<DocumentedType> = window
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
      .await()
}
