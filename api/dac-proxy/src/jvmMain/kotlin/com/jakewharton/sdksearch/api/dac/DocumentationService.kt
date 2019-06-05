package com.jakewharton.sdksearch.api.dac

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import kotlinx.coroutines.Deferred
import retrofit2.http.GET

actual interface DocumentationService {
  @GET("list")
  actual suspend fun list(): List<DocumentedType>
}
