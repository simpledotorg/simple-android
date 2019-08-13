package org.simple.clinic.user

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.LocalAuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class UserSessionAndroidTest {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(LocalAuthenticationRule())
      .around(RxErrorsRule())!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_logging_in_from_registration_entry_user_should_be_logged_in_locally() {
    val selectedFacility = testData.qaFacility()
    val ongoingRegistrationEntry = testData.ongoingRegistrationEntry(
        uuid = testData.qaUserUuid(),
        registrationFacility = selectedFacility
    )
    userSession.saveOngoingRegistrationEntry(ongoingRegistrationEntry)
        .andThen(userSession.saveOngoingRegistrationEntryAsUser())
        .blockingAwait()

    assertThat(userSession.isUserLoggedIn()).isTrue()
    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.WaitingForApproval)

    val currentFacility = facilityRepository
        .currentFacility(testData.qaUser())
        .blockingFirst()
    assertThat(currentFacility.uuid).isEqualTo(selectedFacility.uuid)

    val isRegistrationEntryPresent = userSession.isOngoingRegistrationEntryPresent().blockingGet()
    assertThat(isRegistrationEntryPresent).isTrue()
  }

  @Test
  fun when_user_is_logged_out_then_all_app_data_should_get_cleared() {
    assertThat(userSession.loggedInUser().blockingFirst())
        .isEqualTo(testData.qaUser().toOptional())

    userSession.logout().blockingGet()

    assertThat(userSession.loggedInUser().blockingFirst())
        .isEqualTo(None)
  }

  @Test
  fun when_logged_in_user_is_cleared_the_local_saved_user_must_be_removed_from_database() {
    assertThat(userSession.isUserLoggedIn()).isTrue()

    userSession.clearLoggedInUser().blockingAwait()
    assertThat(userSession.isUserLoggedIn()).isFalse()

    val user = userSession.loggedInUserImmediate()
    assertThat(user).isNull()
  }

  @Test
  fun unauthorizing_the_user_must_work_as_expected() {
    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(LOGGED_IN)

    userSession.unauthorize().blockingAwait()

    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(UNAUTHORIZED)
  }
}
