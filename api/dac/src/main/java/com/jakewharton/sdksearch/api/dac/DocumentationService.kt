package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Url

interface DocumentationService {
  @GET fun list(@Url relativeUrl: String): Deferred<List<Item>>
}
