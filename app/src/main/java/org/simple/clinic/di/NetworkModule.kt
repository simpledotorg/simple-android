package org.simple.clinic.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.analytics.NetworkAnalyticsInterceptor
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.patient.PatientSummaryResult
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.user.LoggedInUserHttpInterceptor
import org.simple.clinic.util.InstantMoshiAdapter
import org.simple.clinic.util.LocalDateMoshiAdapter
import org.simple.clinic.util.MoshiOptionalAdapterFactory
import org.simple.clinic.util.UuidMoshiAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

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
        .add(patientSummaryResultAdapterFactory())
        .add(AppointmentCancelReason.MoshiTypeConverter())
        .build()

    val patientPayloadNullSerializingAdapter = moshi.adapter(PatientPayload::class.java).serializeNulls()

    return moshi
        .newBuilder()
        .add(PatientPayload::class.java, patientPayloadNullSerializingAdapter)
        .build()
  }

  private fun patientSummaryResultAdapterFactory(): PolymorphicJsonAdapterFactory<PatientSummaryResult> {
    return PolymorphicJsonAdapterFactory.of(PatientSummaryResult::class.java, "patient_summary_result")
        .withSubtype(PatientSummaryResult.Scheduled::class.java, "result_scheduled")
        .withSubtype(PatientSummaryResult.Saved::class.java, "result_saved")
  }

  @Provides
  @AppScope
  open fun okHttpClient(
      loggedInInterceptor: LoggedInUserHttpInterceptor,
      appInfoHttpInterceptor: AppInfoHttpInterceptor,
      networkAnalyticsInterceptor: NetworkAnalyticsInterceptor
  ): OkHttpClient {
    return OkHttpClient.Builder()
        .apply {
          addInterceptor(appInfoHttpInterceptor)
          addInterceptor(loggedInInterceptor)
          addInterceptor(networkAnalyticsInterceptor)

          if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            addInterceptor(loggingInterceptor)
          }

          // When syncing large amounts of data, the default read timeout(10s) has been seen to
          // timeout frequently for larger models. Through trial and error, 15s was found to be a
          // good number for syncing large batch sizes.
          readTimeout(15L, TimeUnit.SECONDS)
        }
        .build()
  }

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
  fun retrofit(commonRetrofitBuilder: Retrofit.Builder): Retrofit {
    val baseUrl = BuildConfig.API_ENDPOINT
    return commonRetrofitBuilder
        .baseUrl(baseUrl)
        .build()
  }
}
