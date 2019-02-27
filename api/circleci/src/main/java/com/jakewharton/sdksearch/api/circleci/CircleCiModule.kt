package com.jakewharton.sdksearch.api.circleci

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import retrofit2.Retrofit
import retrofit2.create
import timber.log.Timber
import timber.log.debug

@Module
internal object CircleCiModule {
  @JvmStatic
  @Provides
  fun documentationService(token: String, graphClient: OkHttpClient?): CircleCiService {
    val logger = Timber.tagged("HTTP")
    val client = (graphClient?.newBuilder() ?: Builder())
        .addInterceptor { chain ->
          val request = chain.request()
          val newUrl = request.url()
              .newBuilder()
              .addQueryParameter("circle-token", token)
              .build()
          return@addInterceptor chain.proceed(request.newBuilder()
              .addHeader("Accept", "application/json")
              .url(newUrl)
              .build())
        }
        .addInterceptor(HttpLoggingInterceptor { logger.debug { it } }.setLevel(BASIC))
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://circleci.com/api/v1.1/")
        .client(client)
        .addConverterFactory(Json.nonstrict.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    return retrofit.create()
  }
}
