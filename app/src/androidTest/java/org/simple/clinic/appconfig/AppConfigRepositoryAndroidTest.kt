package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appconfig.StatesResult.StatesFetched
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.CountryV1
import java.util.Optional
import javax.inject.Inject

class AppConfigRepositoryAndroidTest {

  @Inject
  lateinit var appConfigRepository: AppConfigRepository

  @TypedPreference(CountryV1)
  @Inject
  lateinit var countryV1Preference: Preference<Optional<String>>

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    countryV1Preference.delete()
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

  @Test
  fun deleting_the_country_v1_should_delete_it_from_preferences() {
    // given
    val storedCountryJson = """
      |{
      | "country_code": "IN",
      | "endpoint": "https://api.simple.org/api/v1",
      | "display_name": "India",
      | "isd_code": "91"
      |}
    """.trimMargin()
    countryV1Preference.set(Optional.of(storedCountryJson))

    // when
    appConfigRepository.deleteStoredCountryV1()

    // then
    assertThat(countryV1Preference.get()).isEqualTo(Optional.empty<String>())
  }
}
