package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.jakewharton.sdksearch.BuildConfig.CIRCLE_CI_TOKEN
import com.jakewharton.sdksearch.api.circleci.BuildArtifact
import com.jakewharton.sdksearch.api.circleci.CircleCiComponent
import com.jakewharton.sdksearch.api.circleci.Filter.SUCCESSFUL
import com.jakewharton.sdksearch.api.circleci.VcsType.GITHUB
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import timber.log.Timber

class UpdateActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (CIRCLE_CI_TOKEN.isBlank()) {
      Toast.makeText(this, "Circle CI token missing. Cannot update!", LENGTH_SHORT).show()
      finish()
    }

    Toast.makeText(this, "Loading artifacts from Circle CI...", LENGTH_SHORT).show()

    val service = CircleCiComponent.builder()
        .token(CIRCLE_CI_TOKEN)
        .build()
        .service()

    service.listArtifacts(GITHUB, "JakeWharton", "SdkSearch", "master", SUCCESSFUL)
        .flatMapIterable { it }
        .filter { it.nodeIndex == 0 }
        .filter { it.prettyPath.endsWith("build/outputs/apk/debug/sdk-search-debug.apk") }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(object : DisposableObserver<BuildArtifact>() {
          override fun onNext(artifact: BuildArtifact) {
            Timber.d("Found artifact: %s", artifact)

            val uri = Uri.parse(artifact.url)
                .buildUpon()
                .appendQueryParameter("circle-token", CIRCLE_CI_TOKEN)
                .build()
            application.startActivity(Intent(ACTION_VIEW, uri))
          }

          override fun onError(e: Throwable) {
            Timber.i(e, "Failed to fetch artifacts.")
            Toast.makeText(application, "Failed to load artifacts from Circle CI:\n\n${e.message}",
                LENGTH_SHORT).show()
          }
          override fun onComplete() = Unit
        })

    finish()
  }
}
