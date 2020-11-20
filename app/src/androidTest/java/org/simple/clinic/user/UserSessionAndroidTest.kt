package org.simple.clinic.user

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import java.util.UUID
import javax.inject.Inject


class UserSessionAndroidTest {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var selectedCountryPreference: Preference<Optional<Country>>

  @Inject
  lateinit var user: User

  @Inject
  lateinit var facility: Facility

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())
      .around(RxErrorsRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_user_is_logged_out_then_all_app_data_should_get_cleared() {
    userSession.logout().blockingGet()

    assertThat(userSession.loggedInUser().blockingFirst().isPresent()).isFalse()
  }

  @Test
  fun when_logged_in_user_is_cleared_the_local_saved_user_must_be_removed_from_database() {
    assertThat(userSession.isUserPresentLocally()).isTrue()

    userSession.clearLoggedInUser().blockingAwait()
    assertThat(userSession.isUserPresentLocally()).isFalse()

    val user = userSession.loggedInUserImmediate()
    assertThat(user).isNull()
  }

  @Test
  fun unauthorizing_the_user_must_work_as_expected() {
    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(LOGGED_IN)

    userSession.unauthorize().blockingAwait()

    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(UNAUTHORIZED)
  }

  @Test
  fun when_user_is_logged_out_then_the_selected_country_preference_must_not_be_removed() {
    val country = testData.country()
    selectedCountryPreference.set(country.toOptional())

    userSession.logout().blockingGet()

    assertThat(selectedCountryPreference.get()).isEqualTo(Optional.of(country))
  }

  @Test
  fun fetching_the_user_and_facility_details_should_work_as_expected() {
    // given
    val newFacility = TestData.facility(
        uuid = UUID.fromString("56ddc7df-6a81-42bc-8659-78b0ccb51edd"),
        syncGroup = "bb92c082-d749-478a-bd2a-fe7f10055ef0"
    )
    facilityRepository
        .save(listOf(newFacility))
        .blockingAwait()

    facilityRepository.setCurrentFacilityImmediate(newFacility)

    // when
    val userFacilityDetails = userSession.userFacilityDetails()

    // then
    val expected = UserFacilityDetails(
        userId = user.uuid,
        currentFacilityId = newFacility.uuid,
        currentSyncGroupId = newFacility.syncGroup
    )
    assertThat(userFacilityDetails).isEqualTo(expected)
  }
}
