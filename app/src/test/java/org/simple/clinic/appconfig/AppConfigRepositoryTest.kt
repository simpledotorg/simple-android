package org.simple.clinic.appconfig

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.util.ResolvedError
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.URI

class AppConfigRepositoryTest {

  private val api = mock<ManifestFetchApi>()

  private val repository = AppConfigRepository(api)

  @Test
  fun `successful network calls to fetch the app manifest should return the app manifest`() {
    // given
    val countries = listOf(
        Country(code = "IN", endpoint = URI("https://in.simple.org"), displayName = "India", isdCode = "91"),
        Country(code = "BD", endpoint = URI("https://bd.simple.org"), displayName = "Bangladesh", isdCode = "880")
    )
    whenever(api.fetchManifest()).thenReturn(Single.just(countries))

    // then

    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchSucceeded(countries))
        .assertNoErrors()
  }

  @Test
  fun `failed network calls to fetch the app manifest to return an appropriate failure result`() {
    // given
    val cause = ConnectException()
    whenever(api.fetchManifest()).thenReturn(Single.error(cause))

    // then
    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchError(ResolvedError.NetworkRelated(cause)))
        .assertNoErrors()
  }

  @Test
  fun `server errors when fetching the app manifest should return an appropriate failure result`() {
    // given
    val cause = httpException(500)
    whenever(api.fetchManifest()).thenReturn(Single.error(cause))

    // then
    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchError(ResolvedError.ServerError(cause)))
        .assertNoErrors()
  }

  @Test
  fun `unauthenticated errors when fetching the app manifest should return an appropriate failure result`() {
    // given
    val cause = httpException(401)
    whenever(api.fetchManifest()).thenReturn(Single.error(cause))

    // then
    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchError(ResolvedError.Unauthenticated(cause)))
        .assertNoErrors()
  }

  @Test
  fun `any other http error when fetching the app manifest should return an unexpected error`() {
    // given
    val cause = httpException(400)
    whenever(api.fetchManifest()).thenReturn(Single.error(cause))

    // then
    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchError(ResolvedError.Unexpected(cause)))
        .assertNoErrors()
  }

  @Test
  fun `any other error when fetching the app manifest should return an unexpected error`() {
    // given
    val cause = RuntimeException()
    whenever(api.fetchManifest()).thenReturn(Single.error(cause))

    // then
    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchError(ResolvedError.Unexpected(cause)))
        .assertNoErrors()
  }

  private fun httpException(responseCode: Int): HttpException {
    val response = Response.error<String>(
        responseCode,
        ResponseBody.create(MediaType.parse("text/plain"), "FAIL")
    )
    return HttpException(response)
  }

}
