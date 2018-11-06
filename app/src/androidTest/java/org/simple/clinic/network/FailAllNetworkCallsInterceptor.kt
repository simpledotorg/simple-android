package org.simple.clinic.network

import okhttp3.Interceptor
import okhttp3.Response

object FailAllNetworkCallsInterceptor : Interceptor {

  var shouldFailAll = false

  override fun intercept(chain: Interceptor.Chain): Response {
    if (shouldFailAll) {
      throw ForcedException()
    } else {
      return chain.proceed(chain.request())
    }
  }

  class ForcedException : RuntimeException()
}
