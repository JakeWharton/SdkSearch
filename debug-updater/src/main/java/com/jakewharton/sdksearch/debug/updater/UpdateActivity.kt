package com.jakewharton.sdksearch.debug.updater

import android.app.Activity
import android.os.Bundle
import androidx.core.widget.toast

class UpdateActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val config = UpdateConfig(
        BuildConfig.CIRCLE_CI_TOKEN,
        BuildConfig.COMMIT_TIMESTAMP,
        "JakeWharton",
        "SdkSearch",
        "build/commit-timestamp.txt",
        "build/outputs/apk/debug/sdk-search-debug.apk"
    )

    if (config.apiToken.isBlank()) {
      toast("Circle CI token missing. Cannot update!")
    } else {
      startUpdateService(config)
    }

    finish()
  }
}
