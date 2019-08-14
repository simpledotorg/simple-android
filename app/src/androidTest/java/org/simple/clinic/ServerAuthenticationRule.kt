package org.simple.clinic

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.facility.FacilitySyncApi
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import javax.inject.Inject

/** Runs every test with an authenticated user.
 *
 **/
class ServerAuthenticationRule : TestRule {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilitySync: FacilitySync

  @Inject
  lateinit var faker: Faker

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var sharedPreferences: SharedPreferences

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@ServerAuthenticationRule)

        try {
          // Login also needs to happen inside this try block so that in case
          // of a failure, clearData() still gets called to reset all app data.
          register()
          base.evaluate()

        } finally {
          clearData()
        }
      }
    }
  }

  private fun register() {
    fetchFacilities()
    val registerFacilityAt = getFirstStoredFacility()

    val registrationResult = registerUserAtFacility(registerFacilityAt)
    if (registrationResult !is RegistrationResult.Success) {
      throw RuntimeException("Could not register user because: $registrationResult")
    }

    verifyAccessTokenIsPresent()
    verifyUserCanSyncData()
  }

  private fun fetchFacilities() {
    val result = facilitySync
        .pullWithResult()
        .blockingGet()

    assertThat(result).isEqualTo(FacilityPullResult.Success)
  }

  private fun getFirstStoredFacility(): Facility {
    return appDatabase
        .facilityDao()
        .all()
        .blockingFirst()
        .first()
  }

  private fun registerUserAtFacility(facility: Facility): RegistrationResult {
    val registrationEntry = testData.ongoingRegistrationEntry(
        phoneNumber = faker.number.number(10),
        pin = testData.qaUserPin(),
        registrationFacility = facility)

    return userSession.saveOngoingRegistrationEntry(registrationEntry)
        .andThen(userSession.saveOngoingRegistrationEntryAsUser())
        .andThen(userSession.register())
        .blockingGet()
  }

  private fun verifyAccessTokenIsPresent() {
    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()
  }

  private fun verifyUserCanSyncData() {
    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(userSession.isUserLoggedIn()).isTrue()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.ApprovedForSyncing)
    assertThat(loggedInUser.loggedInStatus).isEqualTo(User.LoggedInStatus.LOGGED_IN)
  }

  private fun clearData() {
    sharedPreferences.edit().clear().commit()
    appDatabase.clearPatientData()

    val loggedInUser  = appDatabase.userDao().userImmediate()!!
    appDatabase.userDao().deleteUserAndFacilityMappings(loggedInUser, appDatabase.userFacilityMappingDao())
  }
}
