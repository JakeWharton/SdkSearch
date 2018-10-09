package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.Deferred

expect interface DocumentationService {
  fun list(): Deferred<Map<String, List<Item>>>
}
