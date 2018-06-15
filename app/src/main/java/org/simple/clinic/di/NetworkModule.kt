package org.simple.clinic.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.util.InstantMoshiAdapter
import org.simple.clinic.util.LocalDateMoshiAdapter
import org.simple.clinic.util.UuidMoshiAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

@Module
class NetworkModule {

  @Provides
  fun moshi(): Moshi {
    return Moshi.Builder()
        .add(InstantMoshiAdapter())
        .add(LocalDateMoshiAdapter())
        .add(UuidMoshiAdapter())
        .build()
  }

  @Provides
  fun retrofitBuilder(moshi: Moshi): Retrofit.Builder {
    val okHttpClient = OkHttpClient.Builder()
        .apply {
          if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor({ message -> Timber.tag("OkHttp").d(message) })
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            addNetworkInterceptor(loggingInterceptor)
          }
        }
        .build()

    return Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .validateEagerly(true)
  }
}
