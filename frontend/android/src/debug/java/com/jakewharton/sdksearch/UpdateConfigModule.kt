package com.jakewharton.sdksearch

import com.jakewharton.sdksearch.debug.updater.UpdateConfig
import dagger.Module
import dagger.Provides

@Module
object UpdateConfigModule {
  @JvmStatic @Provides fun provideUpdateConfig() = UpdateConfig(
      BuildConfig.CIRCLE_CI_TOKEN,
      BuildConfig.COMMIT_UNIX_TIMESTAMP,
      "JakeWharton",
      "SdkSearch",
      "build/commit-timestamp.txt",
      "build/outputs/apk/debug/sdk-search-debug.apk"
  )
}
