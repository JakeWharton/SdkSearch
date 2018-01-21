package com.jakewharton.sdksearch.store

import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.content.Context
import com.squareup.sqlbrite3.BriteDatabase
import com.squareup.sqlbrite3.SqlBrite
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import timber.log.Timber

@Module //
internal class DbModule {
  @Provides fun briteDatabase(context: Context, filename: String?, scheduler: Scheduler): BriteDatabase {
    val configuration = Configuration.builder(context)
        .callback(DbCallback())
        .name(filename)
        .build()
    val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
    val logger = SqlBrite.Logger { message -> Timber.tag("DB").d(message) }
    val sqlBrite = SqlBrite.Builder().logger(logger).build()
    return sqlBrite.wrapDatabaseHelper(helper, scheduler)
  }

  @Provides fun itemStore(db: BriteDatabase): ItemStore = SqlItemStore(db)
}

