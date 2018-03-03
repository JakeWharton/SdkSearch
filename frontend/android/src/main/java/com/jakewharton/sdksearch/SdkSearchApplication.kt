package com.jakewharton.sdksearch

import android.annotation.SuppressLint
import android.app.Application
import com.bugsnag.android.Bugsnag
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.store.DbComponent
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import com.jakewharton.timber.bugsnag.BugsnagTree
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SdkSearchApplication : Application() {
  lateinit var baseUrl: BaseUrl
  lateinit var presenter: SearchPresenter

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

    baseUrl = BaseUrl(PRODUCTION_DAC)
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor(
            HttpLoggingInterceptor { Timber.tag("HTTP").d(it) }.setLevel(BASIC))
        .build()

    val service = DacComponent.builder()
        .baseUrl(baseUrl)
        .client(client)
        .build()
        .documentationService()

    val store = DbComponent.builder()
        .context(this)
        .scheduler(Schedulers.io())
        .filename("sdk.db")
        .build()
        .itemStore()

    val synchronizer = ItemSynchronizer(store, service, ITEM_LIST_URL_PATHS)

    presenter = SearchPresenter(UI, store, synchronizer)
  }

  @SuppressLint("SimpleDateFormat") // Explicitly after normalized format not localized.
  private fun formattedCommitTime(): String {
    val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val epochMillis = BuildConfig.COMMIT_UNIX_TIMESTAMP * 1000
    return formatter.format(Date(epochMillis))
  }
}
