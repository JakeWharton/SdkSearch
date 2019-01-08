package com.jakewharton.sdksearch.store.item

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [DbModule.Providers::class])
internal abstract class DbModule {
  @Binds abstract fun itemStore(impl: SqlItemStore): ItemStore

  @Module
  internal object Providers {
    @JvmStatic
    @Provides
    fun itemDatabase(context: Context, filename: String?): ItemDatabase {
      return ItemDatabase(AndroidSqliteDriver(ItemDatabase.Schema, context, filename))
    }

    @JvmStatic
    @Provides
    fun itemQueries(database: ItemDatabase): ItemQueries {
      return database.itemQueries
    }
  }
}
