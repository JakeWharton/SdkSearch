package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred

expect interface DocumentationService {
  fun list(relativeUrl: String): Deferred<List<Item>>
}
