package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred

expect interface DocumentationService {
  fun list(): Deferred<Map<String, List<Item>>>
}
