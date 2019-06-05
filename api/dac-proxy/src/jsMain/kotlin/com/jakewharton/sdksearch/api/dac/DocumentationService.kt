package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType

actual interface DocumentationService {
  actual suspend fun list(): List<DocumentedType>
}
