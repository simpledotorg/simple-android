package org.simple.clinic.user

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySyncApi
import org.simple.clinic.login.LoginResult
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.registration.FindUserResult
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.registration.SaveUserLocallyResult
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.util.RxErrorsRule
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class UserSessionAndroidTest {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilityApi: FacilitySyncApi

  private val authenticationRule = AuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_correct_login_params_are_given_then_login_should_happen_and_session_data_should_be_persisted() {
    val ongoingLoginEntry = userSession.requireLoggedInUser()
        .map { OngoingLoginEntry(uuid = it.uuid, phoneNumber = it.phoneNumber, pin = testData.qaUserPin()) }
        .blockingFirst()

    val lawgon = userSession
        .saveOngoingLoginEntry(ongoingLoginEntry)
        .andThen(userSession.loginWithOtp(testData.qaUserOtp()))
        .blockingGet()

    assertThat(lawgon).isInstanceOf(LoginResult.Success::class.java)
    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()

    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(userSession.isUserLoggedIn()).isTrue()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.ApprovedForSyncing)
    assertThat(loggedInUser.loggedInStatus).isEqualTo(LOGGED_IN)
  }

  @Test
  fun when_incorrect_login_params_are_given_then_login_should_fail() {
    userSession.logout().blockingGet()

    val lawgon = userSession
        .saveOngoingLoginEntry(OngoingLoginEntry(UUID.randomUUID(), "9919299", "0102"))
        .andThen(userSession.loginWithOtp(testData.qaUserOtp()))
        .blockingGet()

    assertThat(lawgon).isInstanceOf(LoginResult.ServerError::class.java)
    assertThat(userSession.isUserLoggedIn()).isFalse()

    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNull()
  }

  @Test
  fun when_logging_in_from_registration_entry_user_should_be_logged_in_locally() {
    userSession.logout().blockingGet()

    val facilities = facilityApi.pull(10)
        .map { it.payloads }
        .map { facilities -> facilities.map { it.toDatabaseModel(SyncStatus.DONE) } }
        .blockingGet()
    appDatabase.facilityDao().save(facilities)

    val selectedFacility = facilities.first()
    val ongoingRegistrationEntry = testData.ongoingRegistrationEntry(registrationFacility = selectedFacility)
    userSession.saveOngoingRegistrationEntry(ongoingRegistrationEntry)
        .andThen(userSession.saveOngoingRegistrationEntryAsUser())
        .blockingAwait()

    assertThat(userSession.isUserLoggedIn()).isTrue()
    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.WaitingForApproval)

    val currentFacility = facilityRepository
        .currentFacility(userSession)
        .blockingFirst()
    assertThat(currentFacility.uuid).isEqualTo(selectedFacility.uuid)

    val isRegistrationEntryPresent = userSession.isOngoingRegistrationEntryPresent().blockingGet()
    assertThat(isRegistrationEntryPresent).isFalse()
  }

  @Test
  fun when_user_is_logged_out_then_all_app_data_should_get_cleared() {
    val ongoingLoginEntry = userSession.requireLoggedInUser()
        .map { OngoingLoginEntry(uuid = it.uuid, phoneNumber = it.phoneNumber, pin = testData.qaUserPin()) }
        .blockingFirst()

    userSession
        .saveOngoingLoginEntry(ongoingLoginEntry)
        .andThen(userSession.loginWithOtp(testData.qaUserOtp()))
        .flatMap { userSession.logout() }
        .blockingGet()

    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(loggedInUser).isNull()
    assertThat(userSession.isUserLoggedIn()).isFalse()
  }

  @Test
  fun when_registering_a_user_is_registered_then_the_logged_in_user_should_be_sent_to_the_server() {
    userSession.logout().blockingGet()

    val facilities = facilityApi.pull(10)
        .map { it.payloads }
        .map { facilities -> facilities.map { it.toDatabaseModel(SyncStatus.DONE) } }
        .blockingGet()
    appDatabase.facilityDao().save(facilities)

    val ongoingRegistrationEntry = testData.ongoingRegistrationEntry(registrationFacility = facilities.first())

    val registrationResult = userSession
        .saveOngoingRegistrationEntry(ongoingRegistrationEntry)
        .andThen(userSession.saveOngoingRegistrationEntryAsUser())
        .andThen(userSession.register())
        .blockingGet()

    assertThat(registrationResult).isInstanceOf(RegistrationResult.Success::class.java)
    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()
  }

  @Test
  fun when_saving_a_user_locally_it_should_save_the_user_locally_with_a_status_of_not_signed_in() {
    val ongoingLoginEntry = userSession.requireLoggedInUser()
        .map { OngoingLoginEntry(uuid = it.uuid, phoneNumber = it.phoneNumber, pin = testData.qaUserPin()) }
        .blockingFirst()

    val findUserResult = userSession.findExistingUser(ongoingLoginEntry.phoneNumber).blockingGet()
    assertThat(findUserResult).isInstanceOf(FindUserResult.Found::class.java)

    val foundUserPayload = (findUserResult as FindUserResult.Found).user
    val result = userSession.syncFacilityAndSaveUser(foundUserPayload).blockingGet()

    assertThat(result).isInstanceOf(SaveUserLocallyResult::class.java)

    val user = appDatabase.userDao().userImmediate()!!
    assertThat(user.uuid).isEqualTo(foundUserPayload.uuid)
    assertThat(user.loggedInStatus).isEqualTo(NOT_LOGGED_IN)

    val facilityUUIDsForUser = appDatabase.userFacilityMappingDao().facilityUuids(user.uuid).blockingFirst()
    assertThat(facilityUUIDsForUser.toSet()).isEqualTo(setOf(foundUserPayload.registrationFacilityId))
  }

  @Test
  fun when_logged_in_user_is_cleared_the_local_saved_user_must_be_removed_from_database() {
    val ongoingLoginEntry = userSession.requireLoggedInUser()
        .map { OngoingLoginEntry(uuid = it.uuid, phoneNumber = it.phoneNumber, pin = testData.qaUserPin()) }
        .blockingFirst()

    val lawgon = userSession
        .saveOngoingLoginEntry(ongoingLoginEntry)
        .andThen(userSession.loginWithOtp(testData.qaUserOtp()))
        .blockingGet()

    assertThat(lawgon).isInstanceOf(LoginResult.Success::class.java)

    assertThat(userSession.isUserLoggedIn()).isTrue()

    userSession.clearLoggedInUser().blockingAwait()
    assertThat(userSession.isUserLoggedIn()).isFalse()

    val user = userSession.loggedInUserImmediate()
    assertThat(user).isNull()
  }

  @Test
  fun when_login_otp_is_requested_successfully_it_must_update_the_logged_in_status_of_the_user() {
    val ongoingLoginEntry = userSession.requireLoggedInUser()
        .map { OngoingLoginEntry(uuid = it.uuid, phoneNumber = it.phoneNumber, pin = testData.qaUserPin()) }
        .blockingFirst()

    val findUserResult = userSession.findExistingUser(ongoingLoginEntry.phoneNumber).blockingGet()
    assertThat(findUserResult).isInstanceOf(FindUserResult.Found::class.java)

    val foundUser = (findUserResult as FindUserResult.Found).user

    val saveLocallyResult = userSession.syncFacilityAndSaveUser(foundUser).blockingGet()
    assertThat(saveLocallyResult).isInstanceOf(SaveUserLocallyResult.Success::class.java)

    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(NOT_LOGGED_IN)

    val requestOtpResult = userSession
        .saveOngoingLoginEntry(ongoingLoginEntry)
        .andThen(userSession.requestLoginOtp())
        .blockingGet()

    assertThat(requestOtpResult).isInstanceOf(LoginResult.Success::class.java)
    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(OTP_REQUESTED)
  }

  @Test
  fun unauthorizing_the_user_must_work_as_expected() {
    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(LOGGED_IN)

    userSession.unauthorize().blockingAwait()

    assertThat(userSession.loggedInUserImmediate()!!.loggedInStatus).isEqualTo(UNAUTHORIZED)
  }
}
