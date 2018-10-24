package org.simple.clinic.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.analytics.NetworkAnalyticsInterceptor
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.user.LoggedInUserHttpInterceptor
import org.simple.clinic.util.InstantMoshiAdapter
import org.simple.clinic.util.LocalDateMoshiAdapter
import org.simple.clinic.util.MoshiOptionalAdapterFactory
import org.simple.clinic.util.UuidMoshiAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
open class NetworkModule {

  @Provides
  @AppScope
  fun moshi(): Moshi {
    val moshi = Moshi.Builder()
        .add(InstantMoshiAdapter())
        .add(LocalDateMoshiAdapter())
        .add(UuidMoshiAdapter())
        .add(MoshiOptionalAdapterFactory())
        .build()

    val medicalHistoryMoshiAdapter = moshi.adapter(MedicalHistoryPayload::class.java).serializeNulls()
    return moshi
        .newBuilder()
        .add(MedicalHistoryPayload::class.java, medicalHistoryMoshiAdapter)
        .build()
  }

  @Provides
  @AppScope
  open fun okHttpClient(loggedInInterceptor: LoggedInUserHttpInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .apply {
          addInterceptor(loggedInInterceptor)
          addInterceptor(NetworkAnalyticsInterceptor())

          if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            addInterceptor(loggingInterceptor)
          }
        }
        .build()
  }

  @Provides
  @AppScope
  fun retrofitBuilder(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit.Builder {
    return Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .validateEagerly(true)
  }

  @Provides
  @AppScope
  fun retrofit(commonRetrofitBuilder: Retrofit.Builder): Retrofit {
    val baseUrl = BuildConfig.API_ENDPOINT
    return commonRetrofitBuilder
        .baseUrl(baseUrl)
        .build()
  }
}
