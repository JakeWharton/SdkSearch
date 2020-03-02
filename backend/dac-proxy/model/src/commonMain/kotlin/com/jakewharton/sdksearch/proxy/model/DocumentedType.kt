package com.jakewharton.sdksearch.proxy.model

import kotlinx.serialization.Serializable
import kotlin.DeprecationLevel.ERROR

@Serializable
data class DocumentedType(
  val packageName: String,
  val className: String,
  val deprecated: Boolean,
  val link: String,
  @Deprecated("Do not use", level = ERROR)
  val api: Int = 0
)
