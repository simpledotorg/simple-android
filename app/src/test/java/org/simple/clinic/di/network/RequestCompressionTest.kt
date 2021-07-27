package org.simple.clinic.di.network

import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSource
import okio.buffer
import okio.gzip
import org.junit.Test
import java.nio.charset.Charset

class RequestCompressionTest {

  private val requestCompression = RequestCompression()

  @Test
  fun `compressing a request body should compress it with GZip`() {
    // given
    val bodyContent = "Hello, World!"

    // when
    val request = createRequest(bodyContent)
    val transformedRequest = requestCompression.compress(request)

    // then
    val buffer = Buffer()
    transformedRequest.body!!.writeTo(buffer)
    val uncompressed = (buffer as BufferedSource).gzip().buffer().readString(Charset.defaultCharset())

    assertThat(uncompressed).isEqualTo(bodyContent)
  }

  @Test
  fun `compressing a request body should add the request header`() {
    // given
    val bodyContent = "Hello, World!"

    // when
    val request = createRequest(bodyContent)
    val transformedRequest = requestCompression.compress(request)

    // then
    assertThat(request.header("Content-Encoding")).isNull()
    assertThat(transformedRequest.header("Content-Encoding")).isEqualTo("gzip")
  }

  private fun createRequest(content: String): Request {
    val body = content.toRequestBody(contentType = "text/plain".toMediaType())

    return Request.Builder()
        .url("http://127.0.0.1/")
        .method("POST", body)
        .build()
  }
}
