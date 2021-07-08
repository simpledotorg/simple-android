package org.simple.clinic.di.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

/**
 * Class that supports GZIP encoding for HTTP request bodies.
 *
 * Copied from https://github.com/square/okhttp/blob/8bb58332dbacdd2bb9beb8e5c813f22508ebeb27/samples/guide/src/main/java/okhttp3/recipes/RequestBodyCompression.java
 **/
class CompressRequestInterceptor @Inject constructor(
    private val requestCompression: RequestCompression
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    val transformedRequest = if (originalRequest.eligibleForCompression)
      requestCompression.compress(originalRequest)
    else
      originalRequest

    return chain.proceed(transformedRequest)
  }
}

private val Request.eligibleForCompression: Boolean
  get() = body != null && header("Content-Encoding") == null
