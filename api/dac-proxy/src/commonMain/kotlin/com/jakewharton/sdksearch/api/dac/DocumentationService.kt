package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType

const val PRODUCTION_PROXY = "https://api.sdksearch.app/"

expect interface DocumentationService {
  suspend fun list(): List<DocumentedType>
}
