package org.simple.clinic.user

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySyncApi
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.user.registeruser.RegistrationResult
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class RegisterUserServerIntegrationTest {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var facilityApi: FacilitySyncApi

  @Inject
  lateinit var registerUser: RegisterUser

  @Inject
  lateinit var sharedPreferences: SharedPreferences

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var passwordHasher: PasswordHasher

  @Inject
  @Named("user_pin")
  lateinit var userPin: String

  @get:Rule
  val ruleChain: RuleChain = Rules.global()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    clearLocalStorage()
  }

  @After
  fun tearDown() {
    clearLocalStorage()
  }

  @Test
  fun registering_a_user_with_the_server_should_save_the_details_in_storage() {
    val facilityDao = appDatabase.facilityDao()
    val registerFacility = fetchOneFacility()

    facilityDao.save(listOf(registerFacility))

    // Intentionally random because the current QA server purge does not purge users, so we
    // *have* to create a new user every time.
    val registerUserWithId = UUID.randomUUID()
    val user = testData.loggedInUser(
        uuid = registerUserWithId,
        pinDigest = passwordHasher.hash(userPin)
    )

    val registrationResult = registerUser.registerUserAtFacility(user, facilityDao.getOne(registerFacility.uuid)!!)
        .blockingGet()

    assertThat(registrationResult).isInstanceOf(RegistrationResult.Success::class.java)
    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()

    val savedUser = userSession.loggedInUserImmediate()!!
    assertThat(savedUser.uuid).isEqualTo(registerUserWithId)
    assertThat(facilityRepository.currentFacility().blockingFirst()).isEqualTo(registerFacility)
  }

  private fun fetchOneFacility(): Facility {
    return facilityApi.pull(1)
        .map { it.payloads }
        .map { facilities -> facilities.map { it.toDatabaseModel(SyncStatus.DONE) } }
        .blockingGet()
        .first()
  }

  private fun clearLocalStorage() {
    appDatabase.clearAllTables()
    sharedPreferences.edit().clear().commit()
  }
}
