package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.api.dac.model.Item
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Url

interface DocumentationService {
  @GET fun list(@Url relativeUrl: String): Deferred<List<Item>>
}
