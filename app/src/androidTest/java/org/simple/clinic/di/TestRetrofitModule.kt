package org.simple.clinic.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.di.network.AppInfoHttpInterceptor
import org.simple.clinic.user.LoggedInUserHttpInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named

@Module
class TestRetrofitModule {

  @Provides
  fun providerInterceptors(
      loggedInInterceptor: LoggedInUserHttpInterceptor,
      appInfoHttpInterceptor: AppInfoHttpInterceptor
  ): List<Interceptor> {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }

    return listOf(loggedInInterceptor, appInfoHttpInterceptor, loggingInterceptor)
  }

  @Provides
  @AppScope
  @Named("for_country")
  fun retrofit(
      moshi: Moshi,
      okHttpClient: OkHttpClient
  ): Retrofit {
    val baseUrl = BuildConfig.FALLBACK_ENDPOINT

    return Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .validateEagerly(true)
        .baseUrl("$baseUrl/")
        .build()
  }
}
