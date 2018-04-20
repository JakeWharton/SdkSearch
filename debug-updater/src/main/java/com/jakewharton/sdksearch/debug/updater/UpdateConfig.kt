package com.jakewharton.sdksearch.debug.updater

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class UpdateConfig(
  val apiToken: String,
  val timestamp: Long,
  val user: String,
  val project: String,
  val timestampPath: String,
  val apkPath: String,
  val branch: String = "master"
) : Parcelable
