package org.simple.clinic.analytics

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class NetworkAnalyticsInterceptor : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val now = System.currentTimeMillis()
    val request = chain.request()
    val response = chain.proceed(request)

    Analytics.reportNetworkCall(
        url = sanitizeUrl(request.url()),
        method = request.method(),
        responseCode = response.code(),
        contentLength = response.header("Content-Length")?.toInt() ?: -1,
        durationMillis = (System.currentTimeMillis() - now).toInt()
    )
    return response
  }

  private fun sanitizeUrl(url: HttpUrl): String {
    val urlString = "${protocol(url)}://${url.host()}${url.encodedPath()}"
    return if (urlString.endsWith('/')) urlString.substringBeforeLast('/') else urlString
  }

  private fun protocol(url: HttpUrl) = if (url.isHttps) "https" else "http"
}
