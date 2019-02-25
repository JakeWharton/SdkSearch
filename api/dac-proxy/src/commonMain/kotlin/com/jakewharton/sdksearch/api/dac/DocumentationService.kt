package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import kotlinx.coroutines.Deferred

const val PRODUCTION_PROXY = "https://api.sdksearch.app/"

expect interface DocumentationService {
  fun list(): Deferred<List<DocumentedType>>
}
