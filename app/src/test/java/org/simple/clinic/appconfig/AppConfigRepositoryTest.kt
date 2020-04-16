package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.ResolvedError
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.URI

class AppConfigRepositoryTest {

  private val manifestFetchApi = mock<ManifestFetchApi>()
  private val selectedCountryPreference = mock<Preference<Optional<Country>>>()

  private val repository = AppConfigRepository(manifestFetchApi, selectedCountryPreference)

  @Test
  fun `successful network calls to fetch the app manifest should return the app manifest`() {
    // given
    val countries = listOf(
        Country(isoCountryCode = "IN", endpoint = URI("https://in.simple.org"), displayName = "India", isdCode = "91"),
        Country(isoCountryCode = "BD", endpoint = URI("https://bd.simple.org"), displayName = "Bangladesh", isdCode = "880")
    )
    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.just(Manifest(countries)))

    // then

    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchSucceeded(countries))
        .assertNoErrors()
  }

  @Test
  fun `failed network calls to fetch the app manifest should return network related failure result`() {
    // given
    val cause = ConnectException()
    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.error(cause))

    // then
    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchError(ResolvedError.NetworkRelated(cause)))
        .assertNoErrors()
  }

  @Test
  fun `server errors when fetching the app manifest should return failure result as server error`() {
    // given
    val cause = httpException(500)
    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.error(cause))

    // then
    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchError(ResolvedError.ServerError(cause)))
        .assertNoErrors()
  }

  @Test
  fun `http 401 errors when fetching the app manifest should return failure result as unauthenticated`() {
    // given
    val cause = httpException(401)
    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.error(cause))

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
    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.error(cause))

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
    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.error(cause))

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

  @Test
  fun `saving the country must save it to local persistence`() {
    // given
    val country = Country(isoCountryCode = "IN", endpoint = URI("https://in.simple.org"), displayName = "India", isdCode = "91")

    // then
    repository
        .saveCurrentCountry(country)
        .test()
        .assertNoErrors()
        .assertComplete()
    verify(selectedCountryPreference).set(Just(country))
    verifyNoMoreInteractions(selectedCountryPreference)
  }
}
