package org.simple.clinic.analytics

import android.net.NetworkCapabilities
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import org.simple.clinic.platform.analytics.Analytics
import java.net.SocketTimeoutException
import javax.inject.Inject

class NetworkAnalyticsInterceptor @Inject constructor(private val networkCapabilitiesProvider: NetworkCapabilitiesProvider) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val now = System.currentTimeMillis()
    val request = chain.request()

    try {
      val response = chain.proceed(request)

      Analytics.reportNetworkCall(
          url = sanitizeUrl(request.url()),
          method = request.method(),
          responseCode = response.code(),
          contentLength = response.header("Content-Length")?.toInt() ?: -1,
          durationMillis = (System.currentTimeMillis() - now).toInt()
      )
      return response
    } catch (e: Throwable) {
      if (e is SocketTimeoutException) {
        networkCapabilitiesProvider.activeNetworkCapabilities()?.let { networkCapabilities ->
          Analytics.reportNetworkTimeout(
              url = sanitizeUrl(request.url()),
              method = request.method(),
              metered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED).not(),
              networkTransportType = Analytics.NetworkTransportType.fromNetworkCapabilities(networkCapabilities),
              downstreamBandwidthKbps = networkCapabilities.linkDownstreamBandwidthKbps,
              upstreamBandwidthKbps = networkCapabilities.linkUpstreamBandwidthKbps
          )
        }
      }

      throw e
    }
  }

  private fun sanitizeUrl(url: HttpUrl): String {
    val urlString = "${protocol(url)}://${url.host()}${url.encodedPath()}"
    return if (urlString.endsWith('/')) urlString.substringBeforeLast('/') else urlString
  }

  private fun protocol(url: HttpUrl) = if (url.isHttps) "https" else "http"
}
