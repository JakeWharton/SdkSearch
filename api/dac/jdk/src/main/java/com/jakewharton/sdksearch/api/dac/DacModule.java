package com.jakewharton.sdksearch.api.dac;

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory;
import com.jakewharton.retrofit2.converter.kotlinx.serialization.KotlinSerializationConverterFactory;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import kotlinx.serialization.json.JSON;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import static java.util.Objects.requireNonNull;

@Module //
abstract class DacModule {
  @Provides @Singleton
  static DocumentationService documentationService(BaseUrl baseUrl, OkHttpClient client) {
    client = client.newBuilder() //
        .addInterceptor(new JavascriptSkippingInterceptor())
        .build();

    MediaType contentType = requireNonNull(MediaType.parse("application/json; charset=utf-8"));
    JSON json = JSON.Companion.getUnquoted();
    Retrofit retrofit = new Retrofit.Builder() //
        .baseUrl(baseUrl.getUrl()) //
        .client(client) //
        .addConverterFactory(
            KotlinSerializationConverterFactory.stringBased(contentType, json::parse, json::stringify)) //
        .addCallAdapterFactory(CoroutineCallAdapterFactory.create()) //
        .build();

    return retrofit.create(DocumentationService.class);
  }
}
