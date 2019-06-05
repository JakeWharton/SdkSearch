package com.jakewharton.sdksearch.api.dac

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

@Module
internal object DacModule {
  @JvmStatic
  @Provides
  fun documentationService(client: OkHttpClient): DocumentationService {
    val contentType = MediaType.get("application/json; charset=utf-8")

    val retrofit = Retrofit.Builder()
        .baseUrl(PRODUCTION_PROXY)
        .client(client)
        .addConverterFactory(Json.asConverterFactory(contentType))
        .build()

    return retrofit.create()
  }
}
