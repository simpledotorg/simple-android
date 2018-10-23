package org.simple.clinic.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.analytics.NetworkAnalyticsInterceptor
import org.simple.clinic.patient.PatientSummaryResult
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
    return Moshi.Builder()
        .add(InstantMoshiAdapter())
        .add(LocalDateMoshiAdapter())
        .add(UuidMoshiAdapter())
        .add(MoshiOptionalAdapterFactory())
        .add(patientSummaryResultAdapterFactory())
        .build()
  }

  private fun patientSummaryResultAdapterFactory(): PolymorphicJsonAdapterFactory<PatientSummaryResult> {
    return PolymorphicJsonAdapterFactory.of(PatientSummaryResult::class.java, "patient_summary_result")
        .withSubtype(PatientSummaryResult.Scheduled::class.java, "result_scheduled")
        .withSubtype(PatientSummaryResult.Saved::class.java, "result_saved")
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
        .addConverterFactory(MoshiConverterFactory.create(moshi).withNullSerialization())
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
