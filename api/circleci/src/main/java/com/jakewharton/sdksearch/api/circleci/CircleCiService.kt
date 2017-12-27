package com.jakewharton.sdksearch.api.circleci

import kotlinx.coroutines.experimental.Deferred
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface CircleCiService {
  @GET("project/{vcs-type}/{user}/{project}/latest/artifacts") //
  fun listArtifacts(
    @Path("vcs-type") vcsType: VcsType,
    @Path("user") user: String,
    @Path("project") project: String,
    @Query("branch") branch: String?,
    @Query("filter") filter: Filter?
  ): Deferred<List<BuildArtifact>>

  @GET @Streaming
  fun getArtifact(@Url path: String): Deferred<ResponseBody>
}
