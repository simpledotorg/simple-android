package org.simple.clinic.di.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.simple.clinic.feature.Feature.HttpRequestBodyCompression
import org.simple.clinic.feature.Features
import javax.inject.Inject

/**
 * Class that supports GZIP encoding for HTTP request bodies.
 *
 * Copied from https://github.com/square/okhttp/blob/8bb58332dbacdd2bb9beb8e5c813f22508ebeb27/samples/guide/src/main/java/okhttp3/recipes/RequestBodyCompression.java
 **/
class CompressRequestInterceptor(
    private val requestCompression: RequestCompression,
    private val requestBodyCompressionEnabled: Boolean
) : Interceptor {

  @Inject
  constructor(
      requestCompression: RequestCompression,
      features: Features
  ) : this(
      requestCompression = requestCompression,
      requestBodyCompressionEnabled = features.isEnabled(HttpRequestBodyCompression)
  )

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    val transformedRequest = if (requestBodyCompressionEnabled && originalRequest.eligibleForCompression)
      requestCompression.compress(originalRequest)
    else
      originalRequest

    return chain.proceed(transformedRequest)
  }
}

private val Request.eligibleForCompression: Boolean
  get() = body != null && header("Content-Encoding") == null
