package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.util.ResolvedError
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.URI
import java.util.Optional

class AppConfigRepositoryTest {

  private val manifestFetchApi = mock<ManifestFetchApi>()
  private val selectedCountryPreference = mock<Preference<Optional<Country_Old>>>()
  private val selectedCountryV2Preference = mock<Preference<Optional<CountryV2>>>()
  private val selectedDeployment = mock<Preference<Optional<Deployment>>>()

  private val repository = AppConfigRepository(
      manifestFetchApi,
      selectedCountryPreference,
      selectedCountryV2Preference,
      selectedDeployment
  )

  @Test
  fun `successful network calls to fetch the app manifest should return the app manifest`() {
    // given
    val countriesV1 = listOf(
        Country_Old(isoCountryCode = "IN", endpoint = URI("https://in.simple.org"), displayName = "India", isdCode = "91"),
        Country_Old(isoCountryCode = "BD", endpoint = URI("https://bd.simple.org"), displayName = "Bangladesh", isdCode = "880")
    )

    val countriesV2 = listOf(
        CountryV2(
            isoCountryCode = "IN",
            displayName = "India",
            isdCode = "91",
            deployments = listOf(
                Deployment(
                    displayName = "IHCI",
                    endPoint = URI("https://in.simple.org")
                )
            )
        )
    )
    val countriesPayload = CountriesPayload(countriesV2)

    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.just(Manifest(countriesV1, countriesPayload)))

    // then

    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchSucceeded(countriesV2))
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
        "FAIL".toResponseBody("text/plain".toMediaType())
    )
    return HttpException(response)
  }

  @Test
  fun `saving the country must save it to local persistence`() {
    // given
    val country = Country_Old(isoCountryCode = "IN", endpoint = URI("https://in.simple.org"), displayName = "India", isdCode = "91")

    // then
    repository
        .saveCurrentCountry(country)
        .test()
        .assertNoErrors()
        .assertComplete()
    verify(selectedCountryPreference).set(Optional.of(country))
    verifyNoMoreInteractions(selectedCountryPreference)
  }

  @Test
  fun `saving the countryV2, must save it to local persistence`() {
    // given
    val country = TestData.countryV2(
        isoCountryCode = "IN",
        displayName = "India",
        isdCode = "91",
        deploymentName = "IHCI",
        deploymentEndPoint = "https://in.simple.org"
    )

    // then
    repository.saveCurrentCountry(country)

    verify(selectedCountryV2Preference).set(Optional.of(country))
    verifyNoMoreInteractions(selectedCountryV2Preference)
  }

  @Test
  fun `saving the deployment, must save it to local persistence`() {
    // given
    val deployment = TestData.deployment(
        displayName = "IHCI",
        endPoint = "https://in.simple.org"
    )

    // when
    repository.saveDeployment(deployment)

    // then
    verify(selectedDeployment).set(Optional.of(deployment))
    verifyNoMoreInteractions(selectedDeployment)
  }
}
