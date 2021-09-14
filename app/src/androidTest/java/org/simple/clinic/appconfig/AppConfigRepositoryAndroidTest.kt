package org.simple.clinic.appconfig

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.StatesResult.StatesFetched
import javax.inject.Inject

class AppConfigRepositoryAndroidTest {

  @Inject
  lateinit var appConfigRepository: AppConfigRepository

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun persisting_selected_country_should_work_correctly() {
    // given
    val country = TestData.country(
        displayName = "India"
    )

    // when
    appConfigRepository.saveCurrentCountry(country)

    // then
    val expected = appConfigRepository.currentCountry()
    assertThat(country).isEqualTo(expected)
  }

  @Test
  fun persisting_deployment_should_work_correctly() {
    // given
    val deployment = TestData.deployment(
        displayName = "IHCI"
    )

    // when
    appConfigRepository.saveDeployment(deployment)

    // then
    val expected = appConfigRepository.currentDeployment()
    assertThat(deployment).isEqualTo(expected)
  }

  @Test
  fun persisting_state_should_work_correctly() {
    // given
    val state = TestData.state(displayName = "Andhra Pradesh")

    // when
    appConfigRepository.saveState(state)

    // then
    val expectedStateName = appConfigRepository.currentState()
    assertThat(expectedStateName).isEqualTo("Andhra Pradesh")
  }

  @Test
  fun fetching_countries_from_manifest_should_work_correctly() {
    // when
    val manifestResult = appConfigRepository
        .fetchAppManifest()
        .blockingGet()

    // then
    assertThat(manifestResult is FetchSucceeded).isTrue()
  }

  @Test
  fun fetching_states_for_selected_country_should_work_correctly() {
    // given
    val country = TestData.country(
        displayName = "India",
        deploymentEndPoint = "https://api.simple.org/api/"
    )

    appConfigRepository.saveCurrentCountry(country)

    // when
    val statesResult = appConfigRepository.fetchStatesInSelectedCountry()

    // then
    assertThat(statesResult is StatesFetched).isTrue()
  }
}
