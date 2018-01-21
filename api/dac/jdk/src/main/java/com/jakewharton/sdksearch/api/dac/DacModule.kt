package com.jakewharton.sdksearch.api.dac

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.stringBased
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.JSON
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.Objects.requireNonNull
import javax.inject.Singleton

@Module //
internal class DacModule {
  @Provides @Singleton fun documentationService(baseUrl: BaseUrl): DocumentationService {
    // TODO logging
    //HttpLoggingInterceptor.Logger logger = message -> Timber.tag("HTTP").d(message);

    val client = OkHttpClient.Builder() //
        .addInterceptor(JavascriptSkippingInterceptor())
        //.addNetworkInterceptor(new HttpLoggingInterceptor(logger).setLevel(BASIC)) //
        .build()

    val contentType = requireNonNull<MediaType>(MediaType.parse("application/json; charset=utf-8"))
    val json = JSON.unquoted
    val retrofit = Retrofit.Builder() //
        .baseUrl(baseUrl.url) //
        .client(client) //
        .addConverterFactory(stringBased(contentType, json::parse, json::stringify))//
        .addCallAdapterFactory(CoroutineCallAdapterFactory()) //
        .build()

    return retrofit.create(DocumentationService::class.java)
  }
}
