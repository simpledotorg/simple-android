package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

private fun <T> throwError(): T {
  throw NotImplementedError()
}

private fun requestBuilder(url: String): Request.Builder = Request.Builder().url(url)

private fun responseBuilder(
    request: Request,
    protocol: Protocol = Protocol.HTTP_1_1,
    code: Int = 200,
    message: String = "OK"
) = Response.Builder()
    .request(request)
    .protocol(protocol)
    .code(code)
    .message(message)

@RunWith(JUnitParamsRunner::class)
class NetworkAnalyticsInterceptorTest {

  private val mockReporter = MockAnalyticsReporter()

  private lateinit var interceptor: NetworkAnalyticsInterceptor

  @Before
  fun setUp() {
    interceptor = NetworkAnalyticsInterceptor()
    Analytics.addReporter(mockReporter)
  }

  @Test
  @Parameters(value = ["0", "100", "200", "300"])
  fun `the request duration must be reported`(requestDuration: Int) {
    val request = requestBuilder("http://simple.org").build()

    val response = responseBuilder(request).build()

    val chain = SuccessfulChain(request, response, requestDuration)

    interceptor.intercept(chain)

    // We use some delta to take into account the sleep duration we specify
    // as well as for actually executing the code
    val networkDurationRange = requestDuration..(requestDuration + 10)

    val reportedDuration = mockReporter.receivedEvents.first().props["durationMs"] as Int
    assertThat(reportedDuration).isIn(networkDurationRange)
  }

  @Test
  @Parameters(value = [
    "http://google.com/search",
    "http://simple.org/test1",
    "https://mail.google.com"
  ])
  fun `the request url must be reported`(url: String) {
    val request = requestBuilder(url).build()

    val response = responseBuilder(request).build()

    val chain = SuccessfulChain(request, response)

    interceptor.intercept(chain)

    val reportedUrl = mockReporter.receivedEvents.first().props["url"] as String

    assertThat(reportedUrl).isEqualTo(url)
  }

  @Test
  @Parameters(value = [
    "https://simple.org|https://simple.org",
    "https://simple.org?|https://simple.org",
    "https://simple.org/test?param1=test1&param2=test2|https://simple.org/test",
    "https://simple.org?param1=test|https://simple.org",
    "https://simple.org/test?param1=|https://simple.org/test"
  ])
  fun `the url that is reported must be stripped of all query parameters`(url: String, expectedUrl: String) {
    val request = requestBuilder(url).build()

    val response = responseBuilder(request).build()

    val chain = SuccessfulChain(request, response)

    interceptor.intercept(chain)

    val reportedUrl = mockReporter.receivedEvents.first().props["url"] as String

    assertThat(reportedUrl).isEqualTo(expectedUrl)
  }

  @Test
  @Parameters(value = [
    "GET|false",
    "POST|true",
    "PUT|true",
    "HEAD|false",
    "DELETE|false"
  ])
  fun `the request method must be reported`(method: String, hasBody: Boolean) {
    var body: RequestBody? = null
    if (hasBody) {
      body = RequestBody.create(MediaType.parse("text/plain"), "Body")
    }

    val request = requestBuilder("https://simple.org")
        .method(method, body)
        .build()

    val response = responseBuilder(request).build()

    val chain = SuccessfulChain(request, response)

    interceptor.intercept(chain)

    val reportedMethod = mockReporter.receivedEvents.first().props["method"] as String

    assertThat(reportedMethod).isEqualTo(method)
  }

  @Test
  @Parameters(value = ["200", "304", "400", "500"])
  fun `the response code must be reported`(responseCode: Int) {
    val request = requestBuilder("https://simple.org").build()

    val response = responseBuilder(request, code = responseCode).build()

    val chain = SuccessfulChain(request, response)

    interceptor.intercept(chain)

    val reportedResponseCode = mockReporter.receivedEvents.first().props["responseCode"] as Int

    assertThat(reportedResponseCode).isEqualTo(responseCode)
  }

  @Test
  @Parameters(value = ["0", "200", "400", "1000"])
  fun `the content length must be reported`(contentLength: Int) {
    val request = requestBuilder("https://simple.org").build()

    val response = responseBuilder(request)
        .header("Content-Length", contentLength.toString())
        .build()

    val chain = SuccessfulChain(request, response)

    interceptor.intercept(chain)

    val reportedContentLength = mockReporter.receivedEvents.first().props["contentLength"] as Int

    assertThat(reportedContentLength).isEqualTo(contentLength)
  }

  @Test
  fun `if the content length is not present, it must be reported as -1`() {
    val request = requestBuilder("https://simple.org").build()

    val response = responseBuilder(request).build()

    val chain = SuccessfulChain(request, response)

    interceptor.intercept(chain)

    val reportedContentLength = mockReporter.receivedEvents.first().props["contentLength"] as Int

    assertThat(reportedContentLength).isEqualTo(-1)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    mockReporter.clear()
  }

  private class SuccessfulChain(val request: Request, val response: Response, val requestTimeMillis: Int = 0) : FakeChain() {

    override fun proceed(request: Request): Response {
      if (requestTimeMillis > 0) {
        Thread.sleep(requestTimeMillis.toLong())
      }
      return response
    }

    override fun request() = request
  }

  private open class FakeChain : Interceptor.Chain {

    override fun writeTimeoutMillis() = throwError<Int>()

    override fun call() = throwError<Call>()

    override fun proceed(request: Request) = throwError<Response>()

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit) = throwError<Interceptor.Chain>()

    override fun connectTimeoutMillis() = throwError<Int>()

    override fun connection() = throwError<Connection?>()

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit) = throwError<Interceptor.Chain>()

    override fun withReadTimeout(timeout: Int, unit: TimeUnit) = throwError<Interceptor.Chain>()

    override fun request() = throwError<Request>()

    override fun readTimeoutMillis() = throwError<Int>()
  }
}

