package com.jakewharton.sdksearch

import android.annotation.SuppressLint
import android.app.Application
import com.bugsnag.android.Bugsnag
import com.jakewharton.timber.bugsnag.BugsnagTree
import dagger.android.HasActivityInjector
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SdkSearchApplication : Application(), HasActivityInjector {
  private lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.IS_CI_BUILD || !BuildConfig.DEBUG) {
      require(BuildConfig.BUGSNAG_API_KEY.isNotBlank()) {
        "Bugsnag API key is blank. Check the README and your Gradle configuration!"
      }

      val client = Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY)

      client.setReleaseStage(BuildConfig.BUILD_TYPE)
      client.setProjectPackages("com.jakewharton.sdksearch")

      client.addToTab("App", "commitTime", formattedCommitTime())
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

    appComponent = createAppComponent()
  }

  override fun activityInjector() = appComponent.activityInjector

  @SuppressLint("SimpleDateFormat") // Explicitly after normalized format not localized.
  private fun formattedCommitTime(): String {
    val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val epochMillis = BuildConfig.COMMIT_UNIX_TIMESTAMP * 1000
    return formatter.format(Date(epochMillis))
  }
}
