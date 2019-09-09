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
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import com.jakewharton.sdksearch.api.circleci.CircleCiComponent
import com.jakewharton.sdksearch.api.circleci.Filter.SUCCESSFUL
import com.jakewharton.sdksearch.api.circleci.VcsType.GITHUB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import timber.log.Timber
import timber.log.debug
import timber.log.info
import timber.log.warn
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

@RestrictTo(LIBRARY) // Public for Android to create.
class UpdateService : Service() {
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val config = intent.getParcelableExtra<UpdateConfig>(KEY_CONFIG)
    if (config == null) {
      Timber.warn { "No config present in intent." }
      return START_NOT_STICKY
    }

    val notifications = getSystemService<NotificationManager>()!!

    if (Build.VERSION.SDK_INT >= 26 && notifications.getNotificationChannel(CHANNEL_ID) == null) {
      val channel = NotificationChannel(CHANNEL_ID, "Debug Updates", IMPORTANCE_LOW)
      notifications.createNotificationChannel(channel)
    }

    startForeground(NOTIFICATION_ID, createNotification(R.string.debug_updater_notification_title_checking,
        R.string.debug_updater_notification_text_checking_1))

    GlobalScope.launch {
      val service = CircleCiComponent.builder()
          .token(config.apiToken)
          .build()
          .service()

      val artifacts = try {
        service.listArtifacts(GITHUB, config.user, config.project, config.branch, SUCCESSFUL)
            .await()
            .filter { it.nodeIndex == 0 }
      } catch (e: IOException) {
        Timber.info(e) { "Failed to fetch artifacts." }
        stopSelf(startId)
        notifications.notify(NOTIFICATION_ID,
            createNotification(R.string.debug_updater_notification_title_failed,
                R.string.debug_updater_notification_text_artifacts_failed))
        return@launch
      }

      val timestampArtifact = artifacts.single { it.prettyPath.endsWith(config.timestampPath) }
      val timestamp = try {
        service.getArtifact(timestampArtifact.url).string().toLong()
      } catch (e: IOException) {
        Timber.info(e) { "Failed to fetch timestamp of latest build." }
        stopSelf(startId)
        notifications.notify(NOTIFICATION_ID,
            createNotification(R.string.debug_updater_notification_title_failed,
                R.string.debug_updater_notification_text_artifacts_failed))
        return@launch
      }

      Timber.debug { "This build: ${config.timestamp}, Latest build: $timestamp" }
      if (timestamp <= config.timestamp) {
        stopSelf(startId)
        launch(Dispatchers.Main) {
          Toast.makeText(this@UpdateService, "App is already up-to-date!", LENGTH_SHORT).show()
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
        val apkResponse = service.getArtifact(apkArtifact.url)

        apkResponse.use { response ->
          apkFile.sink().buffer().use { destination ->
            response.source().readAll(destination)
          }
        }
      } catch (e: IOException) {
        Timber.info(e) { "Failed to download latest build." }
        stopSelf(startId)
        notifications.notify(NOTIFICATION_ID,
            createNotification(R.string.debug_updater_notification_title_failed,
                R.string.debug_updater_notification_text_download_failed))
        return@launch
      }

      Timber.debug { "Downloaded APK to $apkFile" }

      val fileProviderUri = FileProvider.getUriForFile(application,
          "com.jakewharton.sdksearch.updates", apkFile)
      val installIntent = Intent(ACTION_INSTALL_PACKAGE, fileProviderUri)
      installIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
      installIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

      launch(Dispatchers.Main) {
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
