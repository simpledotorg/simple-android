package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appconfig.StatesResult.StatesFetched
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.util.ResolvedError
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.URI
import java.util.Optional

class AppConfigRepositoryTest {

  private val manifestFetchApi = mock<ManifestFetchApi>()
  private val selectedCountryV2Preference = mock<Preference<Optional<Country>>>()
  private val selectedDeployment = mock<Preference<Optional<Deployment>>>()
  private val selectedStatePreference = mock<Preference<Optional<String>>>()
  private val statesFetcher = mock<StatesFetcher>()
  private val countryV1Preference = mock<Preference<Optional<String>>>()

  private val repository = AppConfigRepository(
      manifestFetchApi,
      selectedCountryV2Preference,
      selectedDeployment,
      selectedStatePreference,
      statesFetcher,
      countryV1Preference
  )

  @Test
  fun `successful network calls to fetch the app manifest should return the app manifest`() {
    // given
    val countriesV2 = listOf(
        Country(
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

    whenever(manifestFetchApi.fetchManifest()).doReturn(Single.just(Manifest(countriesPayload)))

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
  fun `saving the country, must save it to local persistence`() {
    // given
    val country = TestData.country(
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

  @Test
  fun `successful network calls to fetch states for selected country, should return the states for all deployments`() {
    // given
    val ihciDeployment = TestData.deployment(
        displayName = "IHCI",
        endPoint = "https://simple.org"
    )
    val keralaDeployment = TestData.deployment(
        displayName = "Kerala",
        endPoint = "https://kerala.simple.org"
    )

    val andhraPradesh = TestData.state(displayName = "Andhra Pradesh", deployment = ihciDeployment)
    val kerala = TestData.state(displayName = "Kerala", deployment = keralaDeployment)

    val states = listOf(andhraPradesh, kerala)

    whenever(selectedCountryV2Preference.get()) doReturn Optional.of(TestData.country(
        displayName = "India",
        deployments = listOf(ihciDeployment, keralaDeployment)
    ))
    whenever(statesFetcher.fetchStates(ihciDeployment)) doReturn listOf(andhraPradesh)
    whenever(statesFetcher.fetchStates(keralaDeployment)) doReturn listOf(kerala)

    // then
    val fetchedStates = repository.fetchStatesInSelectedCountry()
    assertThat(fetchedStates).isEqualTo(StatesFetched(states))
  }

  @Test
  fun `failed network calls to fetch states, should return network related failure result`() {
    // given
    val cause = ConnectException()
    val deployment = TestData.deployment(displayName = "IHCI")

    whenever(selectedCountryV2Preference.get()) doReturn Optional.of(TestData.country(
        displayName = "India",
        deployments = listOf(deployment)
    ))
    whenever(statesFetcher.fetchStates(deployment)) doThrow cause

    // then
    val fetchedStatesResult = repository.fetchStatesInSelectedCountry()
    assertThat(fetchedStatesResult).isEqualTo(StatesResult.FetchError(ResolvedError.NetworkRelated(cause)))
  }

  @Test
  fun `server errors when fetching the states, should return failure result as server error`() {
    // given
    val cause = httpException(500)
    val deployment = TestData.deployment(displayName = "IHCI")

    whenever(selectedCountryV2Preference.get()) doReturn Optional.of(TestData.country(
        displayName = "India",
        deployments = listOf(deployment)
    ))
    whenever(statesFetcher.fetchStates(deployment)) doThrow cause

    // then
    val fetchedStatesResult = repository.fetchStatesInSelectedCountry()
    assertThat(fetchedStatesResult).isEqualTo(StatesResult.FetchError(ResolvedError.ServerError(cause)))
  }

  @Test
  fun `http 401 errors when fetching the states, should return failure result as unauthenticated`() {
    // given
    val cause = httpException(401)
    val deployment = TestData.deployment(displayName = "IHCI")

    whenever(selectedCountryV2Preference.get()) doReturn Optional.of(TestData.country(
        displayName = "India",
        deployments = listOf(deployment)
    ))
    whenever(statesFetcher.fetchStates(deployment)) doThrow cause

    // then
    val fetchedStatesResult = repository.fetchStatesInSelectedCountry()
    assertThat(fetchedStatesResult).isEqualTo(StatesResult.FetchError(ResolvedError.Unauthenticated(cause)))
  }

  @Test
  fun `any other http error when fetching the states, should return an unexpected error`() {
    // given
    val cause = httpException(400)
    val deployment = TestData.deployment(displayName = "IHCI")

    whenever(selectedCountryV2Preference.get()) doReturn Optional.of(TestData.country(
        displayName = "India",
        deployments = listOf(deployment)
    ))
    whenever(statesFetcher.fetchStates(deployment)) doThrow cause

    // then
    val fetchedStatesResult = repository.fetchStatesInSelectedCountry()
    assertThat(fetchedStatesResult).isEqualTo(StatesResult.FetchError(ResolvedError.Unexpected(cause)))
  }

  @Test
  fun `any other error when fetching the states, should return an unexpected error`() {
    // given
    val cause = RuntimeException()
    val deployment = TestData.deployment(displayName = "IHCI")

    whenever(selectedCountryV2Preference.get()) doReturn Optional.of(TestData.country(
        displayName = "India",
        deployments = listOf(deployment)
    ))
    whenever(statesFetcher.fetchStates(deployment)) doThrow cause

    // then
    val fetchedStatesResult = repository.fetchStatesInSelectedCountry()
    assertThat(fetchedStatesResult).isEqualTo(StatesResult.FetchError(ResolvedError.Unexpected(cause)))
  }

  @Test
  fun `saving the state, should save state name to local persistence`() {
    // given
    val deployment = TestData.deployment(
        displayName = "IHCI",
        endPoint = "https://in.simple.org"
    )
    val state = TestData.state(
        displayName = "Andhra Pradesh",
        deployment = deployment
    )

    // when
    repository.saveState(state)

    // then
    verify(selectedStatePreference).set(Optional.of(state.displayName))
    verifyNoMoreInteractions(selectedStatePreference)
  }
}
