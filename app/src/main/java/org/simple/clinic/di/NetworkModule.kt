package org.simple.clinic.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import experiments.ExperimentsServer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.analytics.NetworkAnalyticsInterceptor
import org.simple.clinic.home.patients.illustration.DayOfMonth
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.FirebaseConfigReader
import org.simple.clinic.remoteconfig.FirebaseRemoteConfigCacheExpiration
import org.simple.clinic.user.LoggedInUserHttpInterceptor
import org.simple.clinic.user.UserStatus
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
        .add(AppointmentCancelReason.MoshiTypeConverter())
        .add(Identifier.IdentifierType.MoshiTypeAdapter())
        .add(BusinessId.MetaDataVersion.MoshiTypeAdapter())
        .add(Appointment.AppointmentType.MoshiTypeAdapter())
        .add(UserStatus.MoshiTypeConverter())
        .add(Appointment.Status.MoshiTypeConverter())
        .add(PatientStatus.MoshiTypeAdapter())
        .add(Answer.MoshiTypeAdapter())
        .add(Gender.MoshiTypeAdapter())
        .add(PatientPhoneNumberType.MoshiTypeAdapter())
        .add(DayOfMonth.MoshiTypeAdapter)
        .build()

    val patientPayloadNullSerializingAdapter = moshi.adapter(PatientPayload::class.java).serializeNulls()

    return moshi
        .newBuilder()
        .add(PatientPayload::class.java, patientPayloadNullSerializingAdapter)
        .build()
  }

  @Provides
  @AppScope
  fun okHttpClient(
      loggedInInterceptor: LoggedInUserHttpInterceptor,
      appInfoHttpInterceptor: AppInfoHttpInterceptor,
      networkAnalyticsInterceptor: NetworkAnalyticsInterceptor,
      configReader: ConfigReader,
      moshi: Moshi
  ): OkHttpClient {
    return OkHttpClient.Builder()
        .apply {
          addInterceptor(appInfoHttpInterceptor)
          addInterceptor(loggedInInterceptor)
          addInterceptor(networkAnalyticsInterceptor)
          addInterceptor(ExperimentsServer(moshi))

          if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            addInterceptor(loggingInterceptor)
          }

          // When syncing large amounts of data, the default read timeout(10s) has been seen to
          // timeout frequently for larger models. Through trial and error, 15s was found to be a
          // good number for syncing large batch sizes.
          readTimeout(configReader.long("networkmodule_read_timeout", default = 30L), TimeUnit.SECONDS)
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
    val currentApiVersion = "v3"

    return commonRetrofitBuilder
        .baseUrl("$baseUrl$currentApiVersion/")
        .build()
  }

  @Provides
  @AppScope
  open fun remoteConfig(): FirebaseRemoteConfig {
    return FirebaseRemoteConfig.getInstance()
  }

  @Provides
  fun remoteConfigReader(firebase: FirebaseRemoteConfig): ConfigReader {
    return FirebaseConfigReader(firebase)
  }

  @Provides
  open fun remoteConfigCacheExpiration(): FirebaseRemoteConfigCacheExpiration {
    return FirebaseRemoteConfigCacheExpiration.PRODUCTION
  }
}
