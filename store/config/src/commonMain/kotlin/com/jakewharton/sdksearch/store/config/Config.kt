package com.jakewharton.sdksearch.store.config

const val PRODUCTION_DAC = "https://developer.android.com/"

data class Config(
  val gitWebUrl: String,
  val dacUrl: String
)
