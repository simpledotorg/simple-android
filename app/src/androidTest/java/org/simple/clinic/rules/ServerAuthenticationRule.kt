package org.simple.clinic.rules

import android.app.Application
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import io.bloco.faker.Faker
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.LoginApi
import org.simple.clinic.login.LoginRequest
import org.simple.clinic.login.UserPayload
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import java.io.File
import java.util.UUID
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

  @Inject
  lateinit var application: Application

  @Inject
  lateinit var loginApi: LoginApi

  @Inject
  lateinit var moshi: Moshi

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@ServerAuthenticationRule)
        val cachedUserInformationAdapter = moshi.adapter(CachedUserInformation::class.java)

        try {
          fetchFacilities()
          val cachedUserInformation = readCachedUserInformation(cachedUserInformationAdapter)
          if (cachedUserInformation != null) {
            loginWithPhoneNumber(cachedUserInformation)
          } else {
            register(cachedUserInformationAdapter)
          }
          base.evaluate()

        } finally {
          clearData()
        }
      }
    }
  }

  private fun loginWithPhoneNumber(cachedUserInformation: CachedUserInformation) {
    val loginRequest = LoginRequest(
        UserPayload(
            phoneNumber = cachedUserInformation.phoneNumber,
            pin = testData.qaUserPin(),
            otp = testData.qaUserOtp()
        )
    )
    loginApi
        .requestLoginOtp(cachedUserInformation.userUuid)
        .andThen(loginApi.login(loginRequest))
        .flatMapCompletable(userSession::storeUserAndAccessToken)
        .blockingAwait()

    verifyAccessTokenIsPresent()
    verifyUserCanSyncData()
  }

  private fun register(adapter: JsonAdapter<CachedUserInformation>) {
    val registerFacilityAt = getFirstStoredFacility()

    val registrationResult = registerUserAtFacility(registerFacilityAt)
    if (registrationResult !is RegistrationResult.Success) {
      throw RuntimeException("Could not register user because: $registrationResult")
    }

    verifyAccessTokenIsPresent()
    verifyUserCanSyncData()

    saveRegisteredPhoneNumber(adapter)
  }

  private fun saveRegisteredPhoneNumber(adapter: JsonAdapter<CachedUserInformation>) {
    val savedUser = userSession.loggedInUserImmediate()!!

    temporaryFile().writeText(adapter.toJson(CachedUserInformation(savedUser.uuid, savedUser.phoneNumber)))
  }

  private fun readCachedUserInformation(adapter: JsonAdapter<CachedUserInformation>): CachedUserInformation? {
    return temporaryFile()
        .takeIf { it.exists() && it.length() > 0 }
        ?.readText()
        ?.let(adapter::fromJson)
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
    appDatabase.clearAllTables()
  }

  private fun appVersion(): String {
    val packageManager = application.packageManager
    return packageManager.getPackageInfo(application.packageName, 0).versionName
  }

  private fun temporaryFile() = File(application.cacheDir, "test-${appVersion()}.tmp")

  @JsonClass(generateAdapter = true)
  data class CachedUserInformation(
      val userUuid: UUID,
      val phoneNumber: String
  )
}
