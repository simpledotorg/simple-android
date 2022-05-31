package org.simple.clinic.rules

import android.app.Application
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.components.PhoneNumber
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.sharedTestCode.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.user.registeruser.RegistrationResult
import org.simple.clinic.util.toNullable
import javax.inject.Inject
import javax.inject.Named

private typealias PickRegistrationFacility = (List<Facility>) -> Facility

/**
 * Runs every test with an actual user on the server at a specific facility.
 **/
class ServerRegistrationAtFacilityRule(
    private val pickRegistrationFacility: PickRegistrationFacility
) : TestRule {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilitySync: FacilitySync

  @Inject
  lateinit var fakePhoneNumber: PhoneNumber

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var sharedPreferences: SharedPreferences

  @Inject
  lateinit var application: Application

  @Inject
  lateinit var registerUser: RegisterUser

  @Inject
  lateinit var passwordHasher: PasswordHasher

  @Inject
  @Named("user_pin")
  lateinit var userPin: String

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@ServerRegistrationAtFacilityRule)
        try {
          ensureLoggedInUser()
          base.evaluate()
        } finally {
          clearData()
        }
      }
    }
  }

  private fun ensureLoggedInUser() {
    ensureFacilities()
    register()
  }

  private fun ensureFacilities() {
    val result = facilitySync.pullWithResult()

    assertThat(result).isEqualTo(FacilityPullResult.Success)
  }

  private fun register() {
    val allFacilities = appDatabase.facilityDao().all().blockingFirst()
    val registerFacilityAt = pickRegistrationFacility.invoke(allFacilities)

    val registrationResult = registerUserAtFacility(registerFacilityAt)
    if (registrationResult !is RegistrationResult.Success) {
      throw RuntimeException("Could not register user because: $registrationResult")
    }

    verifyAccessTokenIsPresent()
    verifyUserCanSyncData()
  }

  private fun registerUserAtFacility(facility: Facility): RegistrationResult {
    val user = testData.loggedInUser(
        phone = fakePhoneNumber.phoneNumber(),
        pinDigest = passwordHasher.hash(userPin),
        currentFacilityUuid = facility.uuid,
        registrationFacilityUuid = facility.uuid
    )

    return registerUser.registerUserAtFacility(user).blockingGet()
  }

  private fun verifyAccessTokenIsPresent() {
    val accessToken = userSession.accessToken().toNullable()
    assertThat(accessToken).isNotNull()
  }

  private fun verifyUserCanSyncData() {
    val loggedInUser = userSession.loggedInUser().blockingFirst().get()
    assertThat(userSession.isUserPresentLocally()).isTrue()
    assertThat(loggedInUser.status).isEqualTo(UserStatus.ApprovedForSyncing)
    assertThat(loggedInUser.loggedInStatus).isEqualTo(User.LoggedInStatus.LOGGED_IN)
  }

  private fun clearData() {
    sharedPreferences.edit().clear().commit()
    appDatabase.clearAllTables()
  }
}
