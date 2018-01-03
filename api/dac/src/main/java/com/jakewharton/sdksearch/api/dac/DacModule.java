package com.jakewharton.sdksearch.api.dac;

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory;
import com.squareup.moshi.Moshi;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import timber.log.Timber;

import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

@Module //
abstract class DacModule {
  @Provides @Singleton static DocumentationService documentationService(BaseUrl baseUrl) {
    HttpLoggingInterceptor.Logger logger = message -> Timber.tag("HTTP").d(message);

    OkHttpClient client = new OkHttpClient.Builder() //
        .addInterceptor(new JavascriptSkippingInterceptor())
        .addNetworkInterceptor(new HttpLoggingInterceptor(logger).setLevel(BASIC)) //
        .build();

    Moshi moshi = new Moshi.Builder()
        .add(boolean.class, BooleanString.class, BooleanStringAdapter.INSTANCE)
        .add(DacJsonAdapterFactory.INSTANCE)
        .build();

    Retrofit retrofit = new Retrofit.Builder() //
        .baseUrl(baseUrl.getUrl()) //
        .client(client) //
        // The Moshi converter has to be lenient because the JS keys are unquoted.
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient()) //
        .addCallAdapterFactory(CoroutineCallAdapterFactory.create()) //
        .build();

    return retrofit.create(DocumentationService.class);
  }
}
