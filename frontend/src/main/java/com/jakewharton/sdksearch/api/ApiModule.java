package com.jakewharton.sdksearch.api;

import com.squareup.moshi.Moshi;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import timber.log.Timber;

import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

@Module //
abstract class ApiModule {
  @Provides @Singleton static DocumentationService documentationService(HttpUrl baseUrl) {
    HttpLoggingInterceptor.Logger logger = message -> Timber.tag("HTTP").d(message);

    OkHttpClient client = new OkHttpClient.Builder() //
        .addInterceptor(new JavascriptSkippingInterceptor())
        .addNetworkInterceptor(new HttpLoggingInterceptor(logger).setLevel(BASIC)) //
        .build();

    Moshi moshi = new Moshi.Builder()
        .add(ApiJsonAdapterFactory.INSTANCE)
        .build();

    Retrofit retrofit = new Retrofit.Builder() //
        .baseUrl(baseUrl) //
        .client(client) //
        // The Moshi converter has to be lenient because the JS keys are unquoted.
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient()) //
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync()) //
        .build();

    return retrofit.create(DocumentationService.class);
  }
}
