package com.jakewharton.sdksearch.debug.updater

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_INSTALL_PACKAGE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.jakewharton.sdksearch.api.circleci.CircleCiComponent
import com.jakewharton.sdksearch.api.circleci.Filter.SUCCESSFUL
import com.jakewharton.sdksearch.api.circleci.VcsType.GITHUB
import com.jakewharton.sdksearch.debug.updater.BuildConfig.CIRCLE_CI_TOKEN
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okio.Okio.buffer
import okio.Okio.sink
import timber.log.Timber
import java.io.File
import java.io.IOException

class UpdateActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (CIRCLE_CI_TOKEN.isBlank()) {
      Toast.makeText(this, "Circle CI token missing. Cannot update!", LENGTH_SHORT).show()
    } else {
      startService(Intent(this, UpdateService::class.java))
    }

    finish()
  }
}
