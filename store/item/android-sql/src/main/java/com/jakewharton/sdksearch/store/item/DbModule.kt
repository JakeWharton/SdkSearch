package com.jakewharton.sdksearch.store.item

import android.content.Context
import com.squareup.sqldelight.android.create
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
      return QueryWrapper(QueryWrapper.create(context, filename))
    }

    @JvmStatic
    @Provides
    fun itemQueries(queryWrapper: QueryWrapper): ItemQueries {
      return queryWrapper.itemQueries
    }
  }
}
