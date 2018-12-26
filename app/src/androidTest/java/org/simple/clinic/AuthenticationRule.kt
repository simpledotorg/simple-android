package org.simple.clinic

import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.bp.sync.BloodPressurePushRequest
import org.simple.clinic.bp.sync.BloodPressureSyncApiV1
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilitySyncApiV1
import org.simple.clinic.overdue.AppointmentPushRequest
import org.simple.clinic.overdue.AppointmentSyncApiV1
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.sync.PatientPushRequest
import org.simple.clinic.patient.sync.PatientSyncApiV1
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import java.util.UUID
import javax.inject.Inject

/** Runs every test with an authenticated user.
 *
 * This test can also optionally register a patient and appointment with a specific [UUID]. This is
 * because after introducing changes to the api to restrict syncing to facility groups, certain
 * tests broke because they required a patient/appointment to be present on the server. This allows
 * tests to conveniently register patients and appointments when registering a user.
 **/
class AuthenticationRule(
    val registerPatientWithUuid: UUID? = null,
    val registerAppointmentWithUuid: UUID? = null
) : TestRule {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilityApi: FacilitySyncApiV1

  @Inject
  lateinit var patientSyncApi: PatientSyncApiV1

  // Probably not needed for the purpose of making this hotfix.
  //  @Inject
  //  lateinit var appointmentSyncApiV2: AppointmentSyncApiV2

  @Inject
  lateinit var appointmentSyncApiV1: AppointmentSyncApiV1

  @Inject
  lateinit var bloodPressureSyncApi: BloodPressureSyncApiV1

  @Inject
  lateinit var facilityDao: Facility.RoomDao

  @Inject
  lateinit var faker: Faker

  var registeredFacilityUuid: UUID? = null

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@AuthenticationRule)

        // Cannot figure out why, but we're occasionally seeing failed tests
        // because facility syncing gets called with stale "last-pull" timestamp.
        // As a workaround, the app data will now be cleared before running
        // every test and not after.
        logout()

        try {
          // Login also needs to happen inside this try block so that in case
          // of a failure, logout() still gets called to reset all app data.
          registeredFacilityUuid = register()
          if (registerPatientWithUuid != null) {
            registerPatient(patientUuid = registerPatientWithUuid)
          }
          if (registerAppointmentWithUuid != null) {
            registerAppointment(appointmentUuid = registerAppointmentWithUuid)
          }
          base.evaluate()

        } finally {
          logout()
          registeredFacilityUuid = null
        }
      }
    }
  }

  private fun registerPatient(patientUuid: UUID) {
    val patientPayload = testData.patientPayload(uuid = patientUuid)
    val patientPushRequest = PatientPushRequest(listOf(patientPayload))
    val bloodPressureMeasurementPayload = testData
        .bloodPressureMeasurement(patientUuid = patientUuid, facilityUuid = registeredFacilityUuid!!)
        .toPayload()
    val bloodPressurePushRequest = BloodPressurePushRequest(listOf(bloodPressureMeasurementPayload))

    patientSyncApi
        .push(patientPushRequest)
        .concatWith(bloodPressureSyncApi.push(bloodPressurePushRequest))
        .ignoreElements()
        .blockingAwait()
  }

  private fun registerAppointment(appointmentUuid: UUID) {
    val appointmentPayload = testData.appointmentPayload(uuid = appointmentUuid, facilityUuid = registeredFacilityUuid!!)
    val pushRequest = AppointmentPushRequest(listOf(appointmentPayload))

    appointmentSyncApiV1
        .push(pushRequest)
        .blockingGet()
  }

  private fun register(): UUID {
    val facilities = facilityApi.pull(10)
        .map { it.payloads }
        .map { facilities -> facilities.map { it.toDatabaseModel(SyncStatus.DONE) } }
        .blockingGet()
    facilityDao.save(facilities)

    val registerFacilityAt = facilities.first()

    while (true) {
      val registrationEntry = testData.ongoingRegistrationEntry(
          phoneNumber = faker.number.number(10),
          pin = testData.qaUserPin(),
          facilities = listOf(registerFacilityAt))

      val registrationResult = userSession.saveOngoingRegistrationEntry(registrationEntry)
          .andThen(userSession.loginFromOngoingRegistrationEntry())
          .andThen(userSession.register())
          .blockingGet()

      if (registrationResult is RegistrationResult.Success) {
        break
      }
    }

    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()

    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(userSession.isUserLoggedIn()).isTrue()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.APPROVED_FOR_SYNCING)
    assertThat(loggedInUser.loggedInStatus).isEqualTo(User.LoggedInStatus.LOGGED_IN)

    return registerFacilityAt.uuid
  }

  private fun logout() {
    userSession.logout().blockingAwait()
  }
}
