package org.simple.clinic.di

import android.app.Application
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import java.util.Locale
import javax.inject.Inject

class AppInfoHttpInterceptor @Inject constructor(
    private val userClock: UserClock,
    private val utcClock: UtcClock,
    application: Application
) : Interceptor {

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
        .apply { addTimezoneHeaders(this) }
        .build()

    return chain.proceed(newRequest)
  }

  private fun deviceLanguage(): String {
    return Locale.getDefault().toLanguageTag()
  }

  private fun addTimezoneHeaders(builder: Request.Builder) {
    val currentZoneId = userClock.zone
    val currentZoneOffset = currentZoneId.rules.getOffset(Instant.now(utcClock))

    builder
        .addHeader("X-TIMEZONE-ID", currentZoneId.id)
        .addHeader("X-TIMEZONE-OFFSET", currentZoneOffset.totalSeconds.toString())
  }
}
