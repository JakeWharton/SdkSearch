package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Url

actual interface DocumentationService {
  @GET actual fun list(@Url relativeUrl: String): Deferred<List<Item>>
}
