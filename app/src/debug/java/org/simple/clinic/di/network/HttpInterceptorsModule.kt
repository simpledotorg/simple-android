package org.simple.clinic.di.network

import dagger.Module
import dagger.Provides
import io.sentry.okhttp.SentryOkHttpInterceptor
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import org.simple.clinic.user.LoggedInUserHttpInterceptor

@Module
class HttpInterceptorsModule {

  @Provides
  fun providerInterceptors(
      loggedInInterceptor: LoggedInUserHttpInterceptor,
      appInfoHttpInterceptor: AppInfoHttpInterceptor,
      compressRequestInterceptor: CompressRequestInterceptor
  ): List<Interceptor> {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      level = BODY
    }

    return listOf(
        SentryOkHttpInterceptor(),
        loggedInInterceptor,
        appInfoHttpInterceptor,
        loggingInterceptor,
        compressRequestInterceptor
    )
  }
}
