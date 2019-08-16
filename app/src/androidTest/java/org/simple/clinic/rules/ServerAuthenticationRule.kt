package org.simple.clinic.rules

import android.app.Application
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
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
import org.simple.clinic.util.unsafeLazy
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Runs every test with an actual user on the server.
 *
 * ### How this works
 * This looks for a cached user information that has been registered on a previous test. This is
 * stored in the app's cache directory and will get cleared when the app is uninstalled. If this
 * information does not exist, it registers a new user and stores this information into the cache.
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

  private val cachedUserInformationAdapter by unsafeLazy { moshi.adapter(CachedUserInformation::class.java) }

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@ServerAuthenticationRule)
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
    val cachedUserInformation = readCachedUserInformation()
    if (cachedUserInformation != null) {
      loginWithPhoneNumber(cachedUserInformation.userUuid, cachedUserInformation.phoneNumber)
    } else {
      register()
      cacheRegisteredUserInformation()
    }
  }

  private fun ensureFacilities() {
    val result = facilitySync
        .pullWithResult()
        .blockingGet()

    assertThat(result).isEqualTo(FacilityPullResult.Success)
  }

  private fun readCachedUserInformation(): CachedUserInformation? {
    return temporaryFile()
        .takeIf { it.exists() && it.length() > 0 }
        ?.let { file ->
          try {
            cachedUserInformationAdapter.fromJson(file.readText())
          } catch (e: JsonDataException) {
            // This could potentially happen when running tests locally if the app was not
            // uninstalled and the structure of the cached user information model has changed.
            // In this case, we can register a new user and just cache that again and overwrite
            // the file.
            null
          }
        }
  }

  private fun loginWithPhoneNumber(userUuid: UUID, phoneNumber: String) {
    val loginRequest = LoginRequest(
        UserPayload(
            phoneNumber = phoneNumber,
            pin = testData.qaUserPin(),
            otp = testData.qaUserOtp()
        )
    )

    loginApi
        // Even though the OTP does not change for QA users, the server checks whether an OTP for
        // a user has been consumed when we make the login call.
        .requestLoginOtp(userUuid)
        .andThen(loginApi.login(loginRequest))
        .flatMapCompletable(userSession::storeUserAndAccessToken)
        .blockingAwait()

    verifyAccessTokenIsPresent()
    verifyUserCanSyncData()
  }

  private fun register() {
    val registerFacilityAt = getFirstStoredFacility()

    val registrationResult = registerUserAtFacility(registerFacilityAt)
    if (registrationResult !is RegistrationResult.Success) {
      throw RuntimeException("Could not register user because: $registrationResult")
    }

    verifyAccessTokenIsPresent()
    verifyUserCanSyncData()
  }

  private fun cacheRegisteredUserInformation() {
    val savedUser = userSession.loggedInUserImmediate()!!

    val json = cachedUserInformationAdapter.toJson(CachedUserInformation(savedUser.uuid, savedUser.phoneNumber))
    temporaryFile().writeText(json)
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
