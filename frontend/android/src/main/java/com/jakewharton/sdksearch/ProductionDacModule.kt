package com.jakewharton.sdksearch

import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl

@Module
object ProductionDacModule {
  @JvmStatic @Provides fun provideBaseUrl() = HttpUrl.parse(PRODUCTION_DAC)!!
}
