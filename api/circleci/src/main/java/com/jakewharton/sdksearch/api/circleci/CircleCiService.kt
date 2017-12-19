package com.jakewharton.sdksearch.api.circleci

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CircleCiService {
  @GET("project/{vcs-type}/{user}/{project}/latest/artifacts") //
  fun listArtifacts(
    @Path("vcs-type") vcsType: VcsType,
    @Path("user") user: String,
    @Path("project") project: String,
    @Query("branch") branch: String?,
    @Query("filter") filter: Filter?
  ): Observable<List<BuildArtifact>>
}
