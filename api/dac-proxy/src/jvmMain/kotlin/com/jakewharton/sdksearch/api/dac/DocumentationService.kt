package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import kotlinx.coroutines.Deferred
import retrofit2.http.GET

actual interface DocumentationService {
  @GET("list")
  actual fun list(): Deferred<List<DocumentedType>>
}
