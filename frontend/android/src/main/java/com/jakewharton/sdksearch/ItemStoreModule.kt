package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.sdksearch.store.item.DbComponent
import com.jakewharton.sdksearch.store.item.ItemStore
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import javax.inject.Singleton

@Module
object ItemStoreModule {
  @JvmStatic
  @Provides
  @Singleton
  fun provideItemStore(application: Application): ItemStore {
    return DbComponent.builder()
        .context(application)
        .coroutineContext(Dispatchers.IO)
        .filename("sdk.db") // TODO inject this into module?
        .build()
        .itemStore()
  }
}
