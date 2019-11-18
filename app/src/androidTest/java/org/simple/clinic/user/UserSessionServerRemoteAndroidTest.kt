package org.simple.clinic.user

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilitySyncApi
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.registeruser.RegistrationResult
import org.simple.clinic.rules.ServerAuthenticationRule
import javax.inject.Inject

class UserSessionServerIntegrationTest {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var facilityApi: FacilitySyncApi

  @get:Rule
  val authenticationRule = ServerAuthenticationRule()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_registering_a_user_is_registered_then_the_logged_in_user_should_be_sent_to_the_server() {
    clearLocalDatabase()

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

  private fun clearLocalDatabase() {
    appDatabase.clearAllTables()
  }
}
