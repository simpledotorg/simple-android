package org.simple.clinic.di.network

import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.buffer
import okio.gzip
import javax.inject.Inject

class RequestCompression @Inject constructor() {

  fun compress(request: Request): Request {
    return request
        .newBuilder()
        .header("Content-Encoding", "gzip")
        .method(
            method = request.method,
            body = gzipBody(request.body!!)
        )
        .build()
  }

  private fun gzipBody(body: RequestBody) = GzippedBody(body)

  private class GzippedBody(
      private val source: RequestBody
  ) : RequestBody() {

    override fun contentType() = source.contentType()

    override fun writeTo(sink: BufferedSink) {
      return sink
          .gzip()
          .buffer()
          .use(source::writeTo)
    }
  }
}
