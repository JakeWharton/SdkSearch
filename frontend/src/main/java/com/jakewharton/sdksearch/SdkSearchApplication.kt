package com.jakewharton.sdksearch

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.jakewharton.sdksearch.util.BugsnagTree
import timber.log.Timber
import timber.log.Timber.DebugTree

class SdkSearchApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.IS_CI_BUILD || !BuildConfig.DEBUG) {
      require(BuildConfig.BUGSNAG_API_KEY.isNotBlank()) {
        "Bugsnag API key is blank. Check the README and your Gradle configuration!"
      }

      val client = Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY)!!

      client.setReleaseStage(BuildConfig.BUILD_TYPE)
      client.setProjectPackages("com.jakewharton.sdksearch");
      // TODO client.addToTab("App", "Commit SHA", BuildConfig.COMMIT_SHA);
      // TODO client.addToTab("App", "Commit Time", BuildConfig.COMMIT_TIME);
      // TODO client.addToTab("App", "Build Time", BuildConfig.BUILD_TIME);

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
