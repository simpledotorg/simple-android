package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.simple.clinic.BuildConfig
import org.simple.clinic.analytics.NetworkAnalyticsInterceptor
import org.simple.clinic.user.LoggedInUserHttpInterceptor

@Module
class HttpInterceptorsModule {

  @Provides
  fun providerInterceptors(
      loggedInInterceptor: LoggedInUserHttpInterceptor,
      appInfoHttpInterceptor: AppInfoHttpInterceptor,
      networkAnalyticsInterceptor: NetworkAnalyticsInterceptor
  ): List<Interceptor> {
    return mutableListOf(loggedInInterceptor, appInfoHttpInterceptor, networkAnalyticsInterceptor).apply {
      if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        add(loggingInterceptor)
      }
    }
  }
}
