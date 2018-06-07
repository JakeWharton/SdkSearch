package com.jakewharton.sdksearch.store.item;

import android.content.Context;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.android.SqlDelight;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module //
abstract class DbModule {
  @Provides static QueryWrapper queryWrapper(Context context, @Nullable String filename) {
    return new QueryWrapper(SqlDelight.create(QueryWrapper.Companion, context, filename));
  }
  
  @Provides static ItemQueries itemQueries(QueryWrapper queryWrapper) {
    return queryWrapper.getItemQueries();
  }

  @Binds abstract ItemStore itemStore(SqlItemStore impl);
}
