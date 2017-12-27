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
      finish()
      return
    }

    Toast.makeText(this, "Fetching latest version...", LENGTH_SHORT).show()

    val service = CircleCiComponent.builder()
        .token(CIRCLE_CI_TOKEN)
        .build()
        .service()

    launch {
      val artifacts = try {
        service.listArtifacts(GITHUB, "JakeWharton", "SdkSearch", "master",
            SUCCESSFUL).await()
            .filter { it.nodeIndex == 0 }
      } catch (e: IOException) {
        Timber.i(e, "Failed to fetch artifacts.")
        launch(UI) {
          Toast.makeText(application, "Failed to lookup the latest build:\n\n${e.message}",
              LENGTH_SHORT).show()
        }
        return@launch
      }

      val timestampArtifact = artifacts.single {
        it.prettyPath.endsWith("build/commit-timestamp.txt")
      }
      val timestamp = try {
        service.getArtifact(timestampArtifact.url).await().string().toLong()
      } catch (e: IOException) {
        Timber.i(e, "Failed to fetch timestamp of latest build.")
        launch(UI) {
          Toast.makeText(application, "Failed to lookup the latest build:\n\n${e.message}",
              LENGTH_SHORT).show()
        }
        return@launch
      }

      Timber.d("This build: ${BuildConfig.COMMIT_TIMESTAMP}, Latest build: $timestamp")
      if (timestamp <= BuildConfig.COMMIT_TIMESTAMP) {
        launch(UI) {
          Toast.makeText(this@UpdateActivity, "App is already up-to-date!", LENGTH_SHORT).show()
        }
        return@launch
      }

      val updateDir = File(cacheDir, "updates")
      updateDir.mkdirs()
      val apkFile = File(updateDir, "update.apk")

      val apkArtifact = artifacts.single {
        it.prettyPath.endsWith("build/outputs/apk/debug/sdk-search-debug.apk")
      }
      try {
        val apkResponse = service.getArtifact(apkArtifact.url).await()

        apkResponse.use { response ->
          buffer(sink(apkFile)).use { destination ->
            response.source().readAll(destination)
          }
        }
      } catch (e: IOException) {
        Timber.i(e, "Failed to download latest build.")
        launch(UI) {
          Toast.makeText(application, "Failed to download the latest build:\n\n${e.message}",
              LENGTH_SHORT).show()
        }
        return@launch
      }

      Timber.d("Downloaded APK to $apkFile")

      val fileProviderUri = FileProvider.getUriForFile(application,
          "com.jakewharton.sdksearch.updates", apkFile)
      val intent = Intent(ACTION_INSTALL_PACKAGE, fileProviderUri)
      intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
      intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

      launch(UI) {
        startActivity(intent)
      }
    }

    finish()
  }
}
