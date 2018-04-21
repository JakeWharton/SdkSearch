package com.jakewharton.sdksearch

import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import dagger.Module
import dagger.Provides

@Module
object ProductionDacModule {
  @JvmStatic @Provides fun provideBaseUrl() = BaseUrl(PRODUCTION_DAC)
}
