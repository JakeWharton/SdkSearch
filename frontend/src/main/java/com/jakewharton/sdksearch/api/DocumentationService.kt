package com.jakewharton.sdksearch.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Url

interface DocumentationService {
  @GET fun list(@Url relativeUrl: String): Single<List<ApiItem>>
}
