package com.jakewharton.sdksearch.api.circleci;

import com.squareup.moshi.Moshi;
import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import timber.log.Timber;

import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

@Module //
abstract class CircleCiModule {
  @Provides static CircleCiService documentationService(String token) {
    HttpLoggingInterceptor.Logger logger = message -> Timber.tag("HTTP").d(message);

    OkHttpClient client = new OkHttpClient.Builder() //
        .addInterceptor(chain -> {
          Request request = chain.request();
          HttpUrl newUrl = request.url()
              .newBuilder()
              .addQueryParameter("circle-token", token)
              .build();
          return chain.proceed(request.newBuilder()
              .addHeader("Accept", "application/json")
              .url(newUrl)
              .build());
        })
        .addNetworkInterceptor(new HttpLoggingInterceptor(logger).setLevel(BASIC)) //
        .build();

    Moshi moshi = new Moshi.Builder()
        .add(CircleCiJsonAdapterFactory.INSTANCE)
        .build();

    Retrofit retrofit = new Retrofit.Builder() //
        .baseUrl("https://circleci.com/api/v1.1/") //
        .client(client) //
        // The Moshi converter has to be lenient because the JS keys are unquoted.
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient()) //
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync()) //
        .build();

    return retrofit.create(CircleCiService.class);
  }
}
