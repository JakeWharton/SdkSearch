package com.jakewharton.sdksearch.reference

internal enum class SourceProject(val projectDir: String) {
  LIBCORE("platform/libcore/"),
  BASE("platform/frameworks/base/"),
  CAR("platform/packages/services/Car/"),
  VOIP("platform/frameworks/opt/net/voip/"),
  TELEPHONY("platform/frameworks/opt/telephony/"),
  SUPPORT("platform/frameworks/support/"),
  TESTING("platform/frameworks/testing/"),
  UI_AUTOMATOR("platform/frameworks/uiautomator/"),
  MULTIDEX("platform/frameworks/multidex/"),
  CONSTRAINT_LAYOUT("platform/frameworks/opt/sherpa/"),
  ICU("platform/external/icu/"),
  DATABINDING("platform/frameworks/data-binding/"),
  ;

  init {
    require(projectDir.endsWith('/'))
  }

  fun path(dir: String) = SourceLocation(this, baseDir = dir)
}
