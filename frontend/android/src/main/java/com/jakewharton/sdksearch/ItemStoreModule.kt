package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.sdksearch.store.item.DbComponent
import com.jakewharton.sdksearch.store.item.ItemStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

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
