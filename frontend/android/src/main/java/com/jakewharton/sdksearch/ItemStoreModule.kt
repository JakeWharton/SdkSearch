package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.sdksearch.store.DbComponent
import com.jakewharton.sdksearch.store.ItemStore
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.CommonPool
import javax.inject.Singleton

@Module
object ItemStoreModule {
  @JvmStatic
  @Provides
  @Singleton
  fun provideItemStore(application: Application): ItemStore {
    return DbComponent.builder()
        .context(application)
        .coroutineContext(CommonPool)
        .filename("sdk.db") // TODO inject this into module?
        .build()
        .itemStore()
  }
}
