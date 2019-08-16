package org.simple.clinic.di

import okhttp3.OkHttpClient
import org.simple.clinic.analytics.NetworkAnalyticsInterceptor
import org.simple.clinic.di.AppInfoHttpInterceptor
import org.simple.clinic.di.NetworkModule
import org.simple.clinic.network.FailAllNetworkCallsInterceptor
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.user.LoggedInUserHttpInterceptor

class TestNetworkModule : NetworkModule() {
  override fun okHttpClient(
      loggedInInterceptor: LoggedInUserHttpInterceptor,
      appInfoHttpInterceptor: AppInfoHttpInterceptor,
      networkAnalyticsInterceptor: NetworkAnalyticsInterceptor,
      configReader: ConfigReader
  ): OkHttpClient {
    return super.okHttpClient(loggedInInterceptor, appInfoHttpInterceptor, networkAnalyticsInterceptor, configReader)
        .newBuilder()
        .addInterceptor(FailAllNetworkCallsInterceptor)
        .build()
  }
}
