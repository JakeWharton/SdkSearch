package com.jakewharton.sdksearch.debug.updater

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.jakewharton.sdksearch.debug.updater.BuildConfig.CIRCLE_CI_TOKEN

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
