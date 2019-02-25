package com.jakewharton.sdksearch.proxy.model

import kotlinx.serialization.Serializable

@Serializable
data class DocumentedType(
  val packageName: String,
  val className: String,
  val api: Int,
  val deprecated: Boolean,
  val link: String
)
