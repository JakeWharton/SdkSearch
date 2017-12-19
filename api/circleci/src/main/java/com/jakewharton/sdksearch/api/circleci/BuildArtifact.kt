package com.jakewharton.sdksearch.api.circleci

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class BuildArtifact(
  @Json(name = "pretty_path")
  val prettyPath: String,
  @Json(name = "node_index")
  val nodeIndex: Int,
  val url: String
)
