package org.simple.clinic.di

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.util.InstantMoshiAdapter
import org.simple.clinic.util.LocalDateMoshiAdapter
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UuidMoshiAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named

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
  fun retrofitBuilder(
      moshi: Moshi,
      loggedInUser: Preference<Optional<LoggedInUser>>,
      @Named("preference_access_token") accessToken: Preference<Optional<String>>
  ): Retrofit.Builder {

    val okHttpBuilder = OkHttpClient.Builder()

    if (accessToken.isSet && loggedInUser.isSet) {
      // TODO: add the accessToken header and the userID header here
    }

    if (BuildConfig.DEBUG) {
      val loggingInterceptor = HttpLoggingInterceptor()
      loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
      okHttpBuilder.addInterceptor(loggingInterceptor)
    }

    return Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpBuilder.build())
        .validateEagerly(true)
  }
}
