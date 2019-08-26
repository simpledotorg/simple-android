package org.simple.clinic.di

import android.app.Application
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject

class AppInfoHttpInterceptor @Inject constructor(application: Application): Interceptor {

  private val appVersion: String

  init {
    val packageManager = application.packageManager

    appVersion = packageManager.getPackageInfo(application.packageName, 0).versionName
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    val newRequest = request
        .newBuilder()
        .addHeader("X-APP-VERSION", appVersion)
        .addHeader("Accept-Language", deviceLanguage())
        .build()

    return chain.proceed(newRequest)
  }

  private fun deviceLanguage(): String {
    return Locale.getDefault().toLanguageTag()
  }
}
