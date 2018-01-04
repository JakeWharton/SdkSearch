package com.jakewharton.sdksearch

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.jakewharton.sdksearch.util.BugsnagTree
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

class SdkSearchApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.IS_CI_BUILD || !BuildConfig.DEBUG) {
      require(BuildConfig.BUGSNAG_API_KEY.isNotBlank()) {
        "Bugsnag API key is blank. Check the README and your Gradle configuration!"
      }

      val client = Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY)!!

      client.setReleaseStage(BuildConfig.BUILD_TYPE)
      client.setProjectPackages("com.jakewharton.sdksearch")

      val commitDateTime = Instant.ofEpochSecond(BuildConfig.COMMIT_TIMESTAMP).atOffset(UTC)
      client.addToTab("App", "commitTime", commitDateTime.format(RFC_1123_DATE_TIME))
      client.addToTab("App", "commitSha", BuildConfig.COMMIT_SHA)

      val tree = BugsnagTree(client)
      Timber.plant(tree)
      client.beforeNotify {
        tree.update(it)
        return@beforeNotify true
      }
    }

    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    }
  }
}
