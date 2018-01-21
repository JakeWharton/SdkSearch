package com.jakewharton.sdksearch.api.circleci

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module //
internal class CircleCiModule {
  @Provides fun documentationService(token: String): CircleCiService {
    // TODO logging
    //HttpLoggingInterceptor.Logger logger = message -> Timber.tag("HTTP").d(message);

    val client = OkHttpClient.Builder() //
        .addInterceptor { chain ->
          val request = chain.request()
          val newUrl = request.url()
              .newBuilder()
              .addQueryParameter("circle-token", token)
              .build()
          chain.proceed(request.newBuilder()
              .addHeader("Accept", "application/json")
              .url(newUrl)
              .build())
        }
        //.addNetworkInterceptor(new HttpLoggingInterceptor(logger).setLevel(BASIC)) //
        .build()

    val moshi = Moshi.Builder()
        .add(CircleCiJsonAdapterFactory.INSTANCE)
        .build()

    val retrofit = Retrofit.Builder() //
        .baseUrl("https://circleci.com/api/v1.1/") //
        .client(client) //
        .addConverterFactory(MoshiConverterFactory.create(moshi)) //
        .addCallAdapterFactory(CoroutineCallAdapterFactory()) //
        .build()

    return retrofit.create(CircleCiService::class.java)
  }
}
