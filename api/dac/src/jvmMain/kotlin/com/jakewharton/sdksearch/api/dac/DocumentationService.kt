package com.jakewharton.sdksearch.api.dac

import kotlinx.coroutines.Deferred
import retrofit2.http.GET

actual interface DocumentationService {
  @GET("_s/getsuggestions?p=/&s=irina&c=3")
  actual fun list(): Deferred<Map<String, List<Item>>>
}
