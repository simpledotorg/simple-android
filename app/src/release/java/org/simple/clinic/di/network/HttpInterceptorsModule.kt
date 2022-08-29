package org.simple.clinic.di.network

import com.datadog.android.DatadogInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import org.simple.clinic.analytics.NetworkAnalyticsInterceptor
import org.simple.clinic.user.LoggedInUserHttpInterceptor

@Module
class HttpInterceptorsModule {

  @Provides
  fun providerInterceptors(
      loggedInInterceptor: LoggedInUserHttpInterceptor,
      appInfoHttpInterceptor: AppInfoHttpInterceptor,
      networkAnalyticsInterceptor: NetworkAnalyticsInterceptor,
      compressRequestInterceptor: CompressRequestInterceptor
  ): List<Interceptor> {
    return listOf(
        DatadogInterceptor(),
        loggedInInterceptor,
        appInfoHttpInterceptor,
        compressRequestInterceptor,
        networkAnalyticsInterceptor
    )
  }
}
