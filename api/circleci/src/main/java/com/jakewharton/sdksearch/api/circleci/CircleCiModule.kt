package com.jakewharton.sdksearch.api.circleci

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
internal object CircleCiModule {
  @JvmStatic
  @Provides
  fun documentationService(token: String, graphClient: OkHttpClient?): CircleCiService {
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
        .build()

    val moshi = Moshi.Builder()
        .add(CircleCiJsonAdapterFactory.INSTANCE)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://circleci.com/api/v1.1/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    return retrofit.create<CircleCiService>()
  }
}
