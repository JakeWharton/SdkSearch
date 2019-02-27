package com.jakewharton.sdksearch.api.circleci

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BuildArtifact(
  @SerialName("pretty_path")
  val prettyPath: String,
  @SerialName("node_index")
  val nodeIndex: Int,
  val url: String
)
