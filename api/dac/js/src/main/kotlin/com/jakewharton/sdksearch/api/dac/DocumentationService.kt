package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred

actual interface DocumentationService {
  actual fun list(relativeUrl: String): Deferred<List<Item>>
}
