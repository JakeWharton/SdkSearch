package com.jakewharton.sdksearch.reference

internal enum class SourceProject(val projectDir: String) {
  LIBCORE("platform/libcore/"),
  BASE("platform/frameworks/base/"),
  CAR("platform/packages/services/Car/"),
  VOIP("platform/frameworks/opt/net/voip/"),
  TELEPHONY("platform/frameworks/opt/telephony/"),
  SUPPORT("platform/frameworks/support/"),
  ICU("platform/external/icu/"),
  CONSCRYPT("platform/external/conscrypt/"),
  DATABINDING("platform/frameworks/data-binding/"),
  ;

  init {
    require(projectDir.endsWith('/')) { "Project dir must end with '/': $projectDir" }
  }

  fun path(dir: String) = SourceLocation(this, baseDir = dir)
}
