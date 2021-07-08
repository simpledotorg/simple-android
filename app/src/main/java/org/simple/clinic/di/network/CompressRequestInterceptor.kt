package org.simple.clinic.di.network

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.buffer
import okio.gzip
import javax.inject.Inject

/**
 * Class that supports GZIP encoding for HTTP request bodies.
 *
 * Copied from https://github.com/square/okhttp/blob/8bb58332dbacdd2bb9beb8e5c813f22508ebeb27/samples/guide/src/main/java/okhttp3/recipes/RequestBodyCompression.java
 **/
class CompressRequestInterceptor @Inject constructor() : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    val transformedRequest = if (originalRequest.shouldCompress)
      compress(originalRequest)
    else
      originalRequest

    return chain.proceed(transformedRequest)
  }

  private fun compress(request: Request): Request {
    return request
        .newBuilder()
        .header("Content-Encoding", "gzip")
        .method(
            method = request.method,
            body = gzipBody(request.body!!)
        )
        .build()
  }

  private fun gzipBody(body: RequestBody): RequestBody {
    return object : RequestBody() {
      override fun contentType(): MediaType? {
        return body.contentType()
      }

      override fun writeTo(sink: BufferedSink) {
        sink
            .gzip()
            .buffer()
            .use(body::writeTo)
      }
    }
  }
}

private val Request.shouldCompress: Boolean
  get() = body != null && header("Content-Encoding") == null
