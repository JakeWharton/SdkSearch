package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.store.DbComponent
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import timber.log.Timber
import java.io.File

@Module
object SearchPresenterModule {
  @JvmStatic
  @Provides
  fun provideSearchPresenter(application: Application, baseUrl: BaseUrl): SearchPresenter {
    val cacheDir = application.cacheDir / "http"

    val client = OkHttpClient.Builder()
        .cache(Cache(cacheDir, MEBIBYTES.toBytes(10)))
        .addNetworkInterceptor(
            HttpLoggingInterceptor { Timber.tag("HTTP").d(it) }.setLevel(BASIC))
        .build()

    val service = DacComponent.builder()
        .baseUrl(baseUrl)
        .client(client)
        .build()
        .documentationService()

    val store = DbComponent.builder()
        .context(application)
        .scheduler(Schedulers.io())
        .filename("sdk.db")
        .build()
        .itemStore()

    val synchronizer = ItemSynchronizer(store, service, ITEM_LIST_URL_PATHS)

    return SearchPresenter(UI, store, synchronizer)
  }

  private operator fun File.div(pathSegment: String) = File(this, pathSegment)
}
