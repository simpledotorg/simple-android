package org.simple.clinic.di.network

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.simple.clinic.BuildConfig
import org.simple.clinic.di.AppScope
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named

@Module
class RetrofitModule {

  @Provides
  @AppScope
  fun retrofitBuilder(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit.Builder {
    return Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .validateEagerly(true)
  }

  @Provides
  @AppScope
  @Named("for_config")
  fun configurationRetrofit(commonRetrofitBuilder: Retrofit.Builder): Retrofit {
    return commonRetrofitBuilder
        .baseUrl(BuildConfig.MANIFEST_ENDPOINT)
        .build()
  }

  @Provides
  @AppScope
  @Named("for_country")
  fun retrofit(commonRetrofitBuilder: Retrofit.Builder): Retrofit {
    val baseUrl = BuildConfig.API_ENDPOINT
    val currentApiVersion = "v3"

    return commonRetrofitBuilder
        .baseUrl("$baseUrl$currentApiVersion/")
        .build()
  }
}
