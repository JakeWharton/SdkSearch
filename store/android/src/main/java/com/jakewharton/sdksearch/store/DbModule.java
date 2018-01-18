package com.jakewharton.sdksearch.store;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.content.Context;
import android.support.annotation.Nullable;
import com.squareup.sqlbrite3.BriteDatabase;
import com.squareup.sqlbrite3.SqlBrite;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import timber.log.Timber;

@Module //
abstract class DbModule {
  @Provides static BriteDatabase briteDatabase(Context context, @Nullable String filename, Scheduler scheduler) {
    Configuration configuration = Configuration.builder(context)
        .callback(new DbCallback())
        .name(filename)
        .build();
    SupportSQLiteOpenHelper helper = new FrameworkSQLiteOpenHelperFactory().create(configuration);
    SqlBrite.Logger loger = message -> Timber.tag("DB").d(message);
    SqlBrite sqlBrite = new SqlBrite.Builder().logger(loger).build();
    return sqlBrite.wrapDatabaseHelper(helper, scheduler);
  }

  @Binds abstract ItemStore itemStore(SqlItemStore impl);
}
