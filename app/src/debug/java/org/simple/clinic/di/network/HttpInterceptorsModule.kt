package org.simple.clinic.di.network

import com.datadog.android.DatadogInterceptor
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import dagger.Module
import dagger.Provides
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
      networkPlugin: NetworkFlipperPlugin,
      compressRequestInterceptor: CompressRequestInterceptor
  ): List<Interceptor> {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      level = BODY
    }

    return listOf(
        DatadogInterceptor(),
        loggedInInterceptor,
        appInfoHttpInterceptor,
        loggingInterceptor,
        FlipperOkhttpInterceptor(networkPlugin),
        compressRequestInterceptor
    )
  }
}
