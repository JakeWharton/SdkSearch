package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import kotlinx.coroutines.Deferred

actual interface DocumentationService {
  actual fun list(): Deferred<List<DocumentedType>>
}
