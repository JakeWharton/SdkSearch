package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred

actual interface DocumentationService {
  actual fun list(): Deferred<Map<String, List<Item>>>
}
