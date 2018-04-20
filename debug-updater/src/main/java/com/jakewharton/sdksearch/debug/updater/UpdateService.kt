package com.jakewharton.sdksearch.debug.updater

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_INSTALL_PACKAGE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import androidx.core.content.systemService
import androidx.core.widget.toast
import com.jakewharton.sdksearch.api.circleci.CircleCiComponent
import com.jakewharton.sdksearch.api.circleci.Filter.SUCCESSFUL
import com.jakewharton.sdksearch.api.circleci.VcsType.GITHUB
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okio.Okio.buffer
import okio.Okio.sink
import timber.log.Timber
import java.io.File
import java.io.IOException

private const val KEY_CONFIG = "config"
private const val CHANNEL_ID = "debug-updater"
private const val NOTIFICATION_ID = 1

internal fun Context.startUpdateService(config: UpdateConfig) {
  val intent = Intent(this, UpdateService::class.java)
  intent.putExtra(KEY_CONFIG, config)
  startService(intent)
}

class UpdateService : Service() {
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val config = intent.getParcelableExtra<UpdateConfig>(KEY_CONFIG)
    if (config == null) {
      Timber.w("No config present in intent.")
      return START_NOT_STICKY
    }

    val notifications = systemService<NotificationManager>()

    if (Build.VERSION.SDK_INT >= 26 && notifications.getNotificationChannel(CHANNEL_ID) == null) {
      val channel = NotificationChannel(CHANNEL_ID, "Debug Updates", IMPORTANCE_LOW)
      notifications.createNotificationChannel(channel)
    }

    startForeground(NOTIFICATION_ID, createNotification(R.string.debug_updater_notification_title_checking,
        R.string.debug_updater_notification_text_checking_1))

    launch {
      val service = CircleCiComponent.builder()
          .token(config.apiToken)
          .build()
          .service()

      val artifacts = try {
        service.listArtifacts(GITHUB, config.user, config.project, config.branch,
            SUCCESSFUL).await()
            .filter { it.nodeIndex == 0 }
      } catch (e: IOException) {
        Timber.i(e, "Failed to fetch artifacts.")
        stopSelf(startId)
        notifications.notify(NOTIFICATION_ID,
            createNotification(R.string.debug_updater_notification_title_failed,
                R.string.debug_updater_notification_text_artifacts_failed))
        return@launch
      }

      val timestampArtifact = artifacts.single { it.prettyPath.endsWith(config.timestampPath) }
      val timestamp = try {
        service.getArtifact(timestampArtifact.url).await().string().toLong()
      } catch (e: IOException) {
        Timber.i(e, "Failed to fetch timestamp of latest build.")
        stopSelf(startId)
        notifications.notify(NOTIFICATION_ID,
            createNotification(R.string.debug_updater_notification_title_failed,
                R.string.debug_updater_notification_text_artifacts_failed))
        return@launch
      }

      Timber.d("This build: ${config.timestamp}, Latest build: $timestamp")
      if (timestamp <= config.timestamp) {
        stopSelf(startId)
        launch(UI) {
          toast("App is already up-to-date!")
        }
        return@launch
      }

      notifications.notify(NOTIFICATION_ID,
          createNotification(R.string.debug_updater_notification_title_checking,
              R.string.debug_updater_notification_text_checking_2))

      val updateDir = File(cacheDir, "debug-updates")
      updateDir.mkdirs()
      val apkFile = File(updateDir, "update.apk")

      val apkArtifact = artifacts.single { it.prettyPath.endsWith(config.apkPath) }
      try {
        val apkResponse = service.getArtifact(apkArtifact.url).await()

        apkResponse.use { response ->
          buffer(sink(apkFile)).use { destination ->
            response.source().readAll(destination)
          }
        }
      } catch (e: IOException) {
        Timber.i(e, "Failed to download latest build.")
        stopSelf(startId)
        notifications.notify(NOTIFICATION_ID,
            createNotification(R.string.debug_updater_notification_title_failed,
                R.string.debug_updater_notification_text_download_failed))
        return@launch
      }

      Timber.d("Downloaded APK to $apkFile")

      val fileProviderUri = FileProvider.getUriForFile(application,
          "com.jakewharton.sdksearch.updates", apkFile)
      val installIntent = Intent(ACTION_INSTALL_PACKAGE, fileProviderUri)
      installIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
      installIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

      launch(UI) {
        startActivity(installIntent)
        stopSelf(startId)
      }
    }

    return START_NOT_STICKY
  }

  private fun createNotification(@StringRes titleId: Int, @StringRes textId: Int): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.debug_updater_notification_icon)
        .setContentTitle(getString(titleId))
        .setContentText(getString(textId))
        .build()
  }

  override fun onBind(intent: Intent) = null
}
