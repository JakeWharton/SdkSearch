package com.jakewharton.sdksearch.store.item

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqlDatabase
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
    fun queryWrapper(context: Context, filename: String?): QueryWrapper {
      return QueryWrapper(AndroidSqlDatabase(QueryWrapper.Schema, context, filename))
    }

    @JvmStatic
    @Provides
    fun itemQueries(queryWrapper: QueryWrapper): ItemQueries {
      return queryWrapper.itemQueries
    }
  }
}
