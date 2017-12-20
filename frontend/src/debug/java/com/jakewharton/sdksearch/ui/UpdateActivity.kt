package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_INSTALL_PACKAGE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.jakewharton.sdksearch.BuildConfig.CIRCLE_CI_TOKEN
import com.jakewharton.sdksearch.api.circleci.CircleCiComponent
import com.jakewharton.sdksearch.api.circleci.Filter.SUCCESSFUL
import com.jakewharton.sdksearch.api.circleci.VcsType.GITHUB
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import okio.Okio.buffer
import okio.Okio.sink
import timber.log.Timber
import java.io.File

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

    service.listArtifacts(GITHUB, "JakeWharton", "SdkSearch", "master", SUCCESSFUL)
        .flatMapObservable { Observable.fromIterable(it) }
        .filter { it.nodeIndex == 0 }
        .filter { it.prettyPath.endsWith("build/outputs/apk/debug/sdk-search-debug.apk") }
        .singleOrError()
        .doOnSuccess {
          Timber.d("Found artifact: $it")
        }
        .flatMap { service.getArtifact(it.url) }
        .map { body ->
          val updateDir = File(cacheDir, "updates")
          updateDir.mkdirs()

          val file = File(updateDir, "update.apk")
          body.source().use { source ->
            buffer(sink(file)).use { destination ->
              source.readAll(destination)
            }
          }
          return@map file
        }
        .doOnSuccess { Timber.d("Downloaded APK to $it") }
        .map {
          val uri = FileProvider.getUriForFile(application,
              "com.jakewharton.sdksearch.updates", it)
          val intent = Intent(ACTION_INSTALL_PACKAGE, uri)
          intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
          intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
          return@map intent
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(object : DisposableSingleObserver<Intent>() {
          override fun onSuccess(intent: Intent) = application.startActivity(intent)

          override fun onError(e: Throwable) {
            Timber.i(e, "Failed to fetch artifacts.")
            Toast.makeText(application, "Failed to load artifacts from Circle CI:\n\n${e.message}",
                LENGTH_SHORT).show()
          }
        })

    finish()
  }
}
