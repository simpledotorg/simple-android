package org.simple.clinic.analytics

import android.net.NetworkCapabilities
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
import org.simple.clinic.platform.analytics.Analytics
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
  private val networkCapabilitiesProvider = mock<NetworkCapabilitiesProvider>()
  private val networkCapabilities = mock<NetworkCapabilities>()

  private lateinit var interceptor: NetworkAnalyticsInterceptor

  @Before
  fun setUp() {
    whenever(networkCapabilitiesProvider.activeNetworkCapabilities()).thenReturn(networkCapabilities)
    interceptor = NetworkAnalyticsInterceptor(networkCapabilitiesProvider)
    Analytics.addReporter(mockReporter)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    mockReporter.clear()
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
  fun `the url that is reported must be stripped of all query parameters`(
      url: String,
      expectedUrl: String
  ) {
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

  @Test
  @Parameters(method = "params for failing request with a timeout")
  fun `if the request fails with an exception, it must report it to analytics only if it's a timeout`(
      cause: Throwable,
      shouldReportTimeout: Boolean
  ) {
    val request = requestBuilder("https://simple.org").build()
    val chain = FailingChain(request, cause)

    // Intentionally not using ExpectedException here because it seems to have a problem where it
    // does not fail the test if the assertions fail. We verify the exception manually.
    var thrownException: Throwable? = null
    try {
      interceptor.intercept(chain)
    } catch (e: Throwable) {
      thrownException = e
    }

    if (shouldReportTimeout) {
      val props = mockReporter.receivedEvents.first().props
      assertThat(props["url"] as String).isEqualTo("https://simple.org")
      assertThat(props["method"] as String).isEqualTo("GET")
    } else {
      assertThat(mockReporter.receivedEvents.isEmpty()).isTrue()
    }
    assertThat(thrownException).isSameInstanceAs(cause)
  }

  @Suppress("Unused")
  private fun `params for failing request with a timeout`(): List<List<Any>> {
    return listOf(
        listOf(SocketTimeoutException(), true),
        listOf(RuntimeException(), false),
        listOf(SocketException(), false),
        listOf(UnknownHostException(), false)
    )
  }

  @Test
  fun `if the request fails with a timeout, it must not report it to analytics if there is no active network`() {
    val request = requestBuilder("https://simple.org").build()
    val cause = SocketTimeoutException()
    val chain = FailingChain(request, cause)
    whenever(networkCapabilitiesProvider.activeNetworkCapabilities()).thenReturn(null)

    // Intentionally not using ExpectedException here because it seems to have a problem where it
    // does not fail the test if the assertions fail. We verify the exception manually.
    var thrownException: Throwable? = null
    try {
      interceptor.intercept(chain)
    } catch (e: Throwable) {
      thrownException = e
    }

    assertThat(mockReporter.receivedEvents).isEmpty()
    assertThat(thrownException).isSameInstanceAs(cause)
  }

  @Test
  @Parameters(method = "params for network timeout capabilities")
  fun `when reporting a network timeout, the network capabilities must be reported`(
      meteredConnection: Boolean,
      wifiTransport: Boolean,
      bluetoothTransport: Boolean,
      cellularTransport: Boolean,
      ethernetTransport: Boolean,
      lowPanTransport: Boolean,
      vpnTransport: Boolean,
      wifiAwareTransport: Boolean,
      linkDownstreamKbps: Int,
      linkUpstreamKbps: Int,
      transportType: Analytics.NetworkTransportType
  ) {
    val request = requestBuilder("https://simple.org").build()
    val chain = FailingChain(request, SocketTimeoutException())
    whenever(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)).thenReturn(meteredConnection.not())
    whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(wifiTransport)
    whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)).thenReturn(bluetoothTransport)
    whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(cellularTransport)
    whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).thenReturn(ethernetTransport)
    whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)).thenReturn(lowPanTransport)
    whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)).thenReturn(vpnTransport)
    whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)).thenReturn(wifiAwareTransport)
    whenever(networkCapabilities.linkDownstreamBandwidthKbps).thenReturn(linkDownstreamKbps)
    whenever(networkCapabilities.linkUpstreamBandwidthKbps).thenReturn(linkUpstreamKbps)

    try {
      interceptor.intercept(chain)
    } catch (ignored: Throwable) {
      // Nothing to do here
    }

    val props = mockReporter.receivedEvents.first().props
    assertThat(props["metered"] as Boolean).isEqualTo(meteredConnection)
    assertThat(props["transport"] as Analytics.NetworkTransportType).isEqualTo(transportType)
    assertThat(props["downstreamKbps"] as Int).isEqualTo(linkDownstreamKbps)
    assertThat(props["upstreamKbps"] as Int).isEqualTo(linkUpstreamKbps)
  }

  @Suppress("Unused")
  private fun `params for network timeout capabilities`(): List<List<Any>> {
    fun testCase(
        meteredConnection: Boolean,
        wifiTransport: Boolean,
        bluetoothTransport: Boolean,
        cellularTransport: Boolean,
        ethernetTransport: Boolean,
        lowPanTransport: Boolean,
        vpnTransport: Boolean,
        wifiAwareTransport: Boolean,
        linkDownstreamKbps: Int,
        linkUpstreamKbps: Int,
        transportType: Analytics.NetworkTransportType
    ): List<Any> {
      return listOf(
          meteredConnection,
          wifiTransport,
          bluetoothTransport,
          cellularTransport,
          ethernetTransport,
          lowPanTransport,
          vpnTransport,
          wifiAwareTransport,
          linkDownstreamKbps,
          linkUpstreamKbps,
          transportType)
    }

    return listOf(
        testCase(
            meteredConnection = false,
            wifiTransport = true,
            bluetoothTransport = false,
            cellularTransport = false,
            ethernetTransport = false,
            lowPanTransport = false,
            vpnTransport = false,
            wifiAwareTransport = false,
            linkDownstreamKbps = 150,
            linkUpstreamKbps = 100,
            transportType = Analytics.NetworkTransportType.WIFI
        ),
        testCase(
            meteredConnection = true,
            wifiTransport = false,
            bluetoothTransport = false,
            cellularTransport = true,
            ethernetTransport = false,
            lowPanTransport = false,
            vpnTransport = false,
            wifiAwareTransport = false,
            linkDownstreamKbps = 750,
            linkUpstreamKbps = 1000,
            transportType = Analytics.NetworkTransportType.CELLULAR
        ),
        testCase(
            meteredConnection = false,
            wifiTransport = false,
            bluetoothTransport = true,
            cellularTransport = false,
            ethernetTransport = false,
            lowPanTransport = false,
            vpnTransport = false,
            wifiAwareTransport = false,
            linkDownstreamKbps = 1500,
            linkUpstreamKbps = 10,
            transportType = Analytics.NetworkTransportType.BLUETOOTH
        ),
        testCase(
            meteredConnection = true,
            wifiTransport = false,
            bluetoothTransport = false,
            cellularTransport = false,
            ethernetTransport = false,
            lowPanTransport = false,
            vpnTransport = false,
            wifiAwareTransport = false,
            linkDownstreamKbps = 0,
            linkUpstreamKbps = 0,
            transportType = Analytics.NetworkTransportType.OTHER
        )
    )
  }


  private class SuccessfulChain(
      val request: Request,
      val response: Response,
      val requestTimeMillis: Int = 0
  ) : FakeChain() {

    override fun proceed(request: Request): Response {
      if (requestTimeMillis > 0) {
        Thread.sleep(requestTimeMillis.toLong())
      }
      return response
    }

    override fun request() = request
  }

  private class FailingChain(val request: Request, val throwable: Throwable) : FakeChain() {

    override fun proceed(request: Request): Response {
      throw throwable
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

