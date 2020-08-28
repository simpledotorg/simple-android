package org.simple.clinic.overdue

import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.OverduePatientAddress
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.Appointment.Status.Visited
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Rules
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.toNullable
import org.simple.clinic.util.toUtcInstant
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject


class AppointmentRepositoryAndroidTest {

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var bpRepository: BloodPressureRepository

  @Inject
  lateinit var bloodSugarRepository: BloodSugarRepository

  @Inject
  lateinit var medicalHistoryRepository: MedicalHistoryRepository

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var userClock: TestUserClock

  @Inject
  lateinit var user: User

  @Inject
  lateinit var facility: Facility

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())

  private val patientUuid = UUID.fromString("fcf0acd3-0b09-4ecb-bcd4-af40ca6456fc")
  private val appointmentUuid = UUID.fromString("a374e38f-6bc3-4829-899c-0966a4e13b10")

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.parse("2018-01-01"))
    userClock.setDate(LocalDate.parse("2018-01-01"))
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }

  @Test
  fun when_creating_new_appointment_then_the_appointment_should_be_saved() {
    // given
    val appointmentDate = LocalDate.now(clock)
    val creationFacility = testData.facility(uuid = UUID.fromString("4e32c8c8-cfa0-4665-a52e-7398e47aa8b9"))

    //when
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentDate,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = creationFacility.uuid
    )

    // then
    val savedAppointment = getAppointmentByUuid(appointmentUuid)
    with(savedAppointment) {
      assertThat(patientUuid).isEqualTo(this@AppointmentRepositoryAndroidTest.patientUuid)
      assertThat(scheduledDate).isEqualTo(appointmentDate)
      assertThat(status).isEqualTo(Scheduled)
      assertThat(remindOn).isNull()
      assertThat(cancelReason).isNull()
      assertThat(agreedToVisit).isNull()
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(creationFacilityUuid).isEqualTo(creationFacility.uuid)
    }
  }

  @Test
  fun when_creating_new_appointment_then_all_old_appointments_for_that_patient_should_be_marked_as_visited() {
    // given
    val firstAppointmentUuid = UUID.fromString("0bc9cdb3-bfe9-41e9-88b9-2a072c748c47")
    val scheduledDateOfFirstAppointment = LocalDate.parse("2018-01-01")
    val firstAppointmentScheduledAtTimestamp = Instant.now(clock)
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = firstAppointmentUuid,
        appointmentDate = scheduledDateOfFirstAppointment,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )
    markAppointmentSyncStatusAsDone(firstAppointmentUuid)

    clock.advanceBy(Duration.ofHours(24))

    val secondAppointmentUuid = UUID.fromString("ed31c3ae-8903-45fe-9ad3-0302dcba7fc6")
    val scheduleDateOfSecondAppointment = LocalDate.parse("2018-02-01")
    val secondAppointmentScheduledAtTimestamp = Instant.now(clock)

    // when
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = secondAppointmentUuid,
        appointmentDate = scheduleDateOfSecondAppointment,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )

    // then
    val firstAppointment = getAppointmentByUuid(firstAppointmentUuid)
    with(firstAppointment) {
      assertThat(patientUuid).isEqualTo(this@AppointmentRepositoryAndroidTest.patientUuid)
      assertThat(scheduledDate).isEqualTo(scheduledDateOfFirstAppointment)
      assertThat(status).isEqualTo(Visited)
      assertThat(cancelReason).isEqualTo(null)
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(createdAt).isEqualTo(firstAppointmentScheduledAtTimestamp)
      assertThat(createdAt).isLessThan(secondAppointmentScheduledAtTimestamp)
      assertThat(updatedAt).isEqualTo(secondAppointmentScheduledAtTimestamp)
    }

    val secondAppointment = getAppointmentByUuid(secondAppointmentUuid)
    with(secondAppointment) {
      assertThat(patientUuid).isEqualTo(this@AppointmentRepositoryAndroidTest.patientUuid)
      assertThat(scheduledDate).isEqualTo(scheduleDateOfSecondAppointment)
      assertThat(status).isEqualTo(Scheduled)
      assertThat(cancelReason).isEqualTo(null)
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(createdAt).isEqualTo(secondAppointmentScheduledAtTimestamp)
      assertThat(updatedAt).isEqualTo(secondAppointmentScheduledAtTimestamp)
    }
  }

  @Test
  fun deleted_blood_pressure_measurements_should_not_be_considered_when_fetching_overdue_appointments() {
    fun createBloodPressure(
        bpUuid: UUID,
        patientUuid: UUID,
        recordedAt: Instant,
        deletedAt: Instant? = null
    ): BloodPressureMeasurement {
      return testData.bloodPressureMeasurement(
          uuid = bpUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          userUuid = user.uuid,
          syncStatus = DONE,
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          recordedAt = recordedAt,
          deletedAt = deletedAt
      )
    }

    fun createAppointment(patientUuid: UUID, scheduledDate: LocalDate): Appointment {
      return testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          status = Scheduled,
          scheduledDate = scheduledDate)
    }

    // given
    val noBpsDeletedPatientUuid = UUID.fromString("d05b8ed2-97ae-4fda-8af9-bc4168af3c4d")
    val latestBpDeletedPatientUuid = UUID.fromString("9e5ec219-f4a5-4bab-9283-0a087c5d7ac2")
    val oldestBpNotDeletedPatientUuid = UUID.fromString("54e7143c-fe64-4cd8-8c92-f379a79a60f9")
    val allBpsDeletedPatientUuid = UUID.fromString("05bd9d55-5742-466f-b97e-07301e25fe7e")

    val patients = listOf(
        testData.patientProfile(
            patientUuid = noBpsDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "No BPs are deleted"
        ),
        testData.patientProfile(
            patientUuid = latestBpDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "Latest BP is deleted"
        ),
        testData.patientProfile(
            patientUuid = oldestBpNotDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "Oldest BP is not deleted"
        ),
        testData.patientProfile(
            patientUuid = allBpsDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "All BPs are deleted"
        )
    )

    patientRepository.save(patients).blockingAwait()

    val bpsForPatientWithNoBpsDeleted = listOf(
        createBloodPressure(
            bpUuid = UUID.fromString("189b0842-044e-4f1c-a214-24318052f11d"),
            patientUuid = noBpsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-01T00:00:00Z")
        ),
        createBloodPressure(
            bpUuid = UUID.fromString("ce5deb11-05ee-4f9e-8734-ec3d99f271a9"),
            patientUuid = noBpsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-02T00:00:00Z")
        )
    )

    val bpsForPatientWithLatestBpDeleted = listOf(
        createBloodPressure(
            bpUuid = UUID.fromString("55266e25-0c15-4cd3-969d-3c5d5af48c62"),
            patientUuid = latestBpDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-03T00:00:00Z")
        ),
        createBloodPressure(
            bpUuid = UUID.fromString("e4c3461e-8624-4b6e-874b-bb73967e423e"),
            patientUuid = latestBpDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-04T00:00:00Z")
        ),
        createBloodPressure(
            bpUuid = UUID.fromString("e7d19558-36d8-4b5a-a17a-6e3117622b57"),
            patientUuid = latestBpDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-05T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-05T00:00:00Z")
        )
    )

    val bpsForPatientWithOldestBpNotDeleted = listOf(
        createBloodPressure(
            bpUuid = UUID.fromString("1de759ae-9f60-4be5-a1f1-d18143bf8318"),
            patientUuid = oldestBpNotDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-06T00:00:00Z")
        ),
        createBloodPressure(
            bpUuid = UUID.fromString("f135aaa8-e4d6-48c0-acbf-ed0938c44f34"),
            patientUuid = oldestBpNotDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-07T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-07T00:00:00Z")
        ),
        createBloodPressure(
            bpUuid = UUID.fromString("44cff8a9-08c2-4a48-9f4b-5c1ec7d9c10c"),
            patientUuid = oldestBpNotDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-08T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-08T00:00:00Z")
        )
    )

    val bpsForPatientWithAllBpsDeleted = listOf(
        createBloodPressure(
            bpUuid = UUID.fromString("264c4295-c61b-41df-8548-460977510574"),
            patientUuid = allBpsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-09T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-09T00:00:00Z")
        ),
        createBloodPressure(
            bpUuid = UUID.fromString("ff2a665e-d09a-4110-9791-8e966690370f"),
            patientUuid = allBpsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-10T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-10T00:00:00Z")
        ),
        createBloodPressure(
            bpUuid = UUID.fromString("4e97bd7e-87ea-4d4c-a826-3784703937ed"),
            patientUuid = allBpsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-11T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-11T00:00:00Z")
        )
    )

    bpRepository
        .save(bpsForPatientWithNoBpsDeleted + bpsForPatientWithLatestBpDeleted + bpsForPatientWithOldestBpNotDeleted + bpsForPatientWithAllBpsDeleted)
        .blockingAwait()

    val today = LocalDate.now(clock)
    val appointmentsScheduledFor = today.minusDays(1L)

    val appointmentForPatientWithNoBpsDeleted = createAppointment(
        patientUuid = noBpsDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    val appointmentForPatientWithLatestBpDeleted = createAppointment(
        patientUuid = latestBpDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    val appointmentsForPatientWithOldestBpNotDeleted = createAppointment(
        patientUuid = oldestBpNotDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    val appointmentsForPatientWithAllBpsDeleted = createAppointment(
        patientUuid = allBpsDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    appointmentRepository
        .save(listOf(appointmentForPatientWithNoBpsDeleted, appointmentForPatientWithLatestBpDeleted, appointmentsForPatientWithOldestBpNotDeleted, appointmentsForPatientWithAllBpsDeleted))
        .blockingAwait()

    // when
    val overdueAppointments = appointmentRepository.overdueAppointments(since = today, facility = facility).blockingFirst()
        .associateBy { it.appointment.patientUuid }

    // then
    assertThat(overdueAppointments.keys).containsExactly(noBpsDeletedPatientUuid, latestBpDeletedPatientUuid, oldestBpNotDeletedPatientUuid)

    val lastSeenForNoBpsDeletedPatient = overdueAppointments.getValue(noBpsDeletedPatientUuid).patientLastSeen
    val lastSeenForLatestBpDeletedPatient = overdueAppointments.getValue(latestBpDeletedPatientUuid).patientLastSeen
    val lastSeenForOldestBpDeletedPatient = overdueAppointments.getValue(oldestBpNotDeletedPatientUuid).patientLastSeen

    assertThat(lastSeenForNoBpsDeletedPatient).isEqualTo(bpsForPatientWithNoBpsDeleted[1].recordedAt)
    assertThat(lastSeenForLatestBpDeletedPatient).isEqualTo(bpsForPatientWithLatestBpDeleted[1].recordedAt)
    assertThat(lastSeenForOldestBpDeletedPatient).isEqualTo(bpsForPatientWithOldestBpNotDeleted[0].recordedAt)
  }

  @Test
  fun deleted_blood_sugar_measurements_should_not_be_considered_when_fetching_overdue_appointments() {
    fun createBloodSugar(
        bpUuid: UUID,
        patientUuid: UUID,
        recordedAt: Instant,
        deletedAt: Instant? = null
    ): BloodSugarMeasurement {
      return testData.bloodSugarMeasurement(
          uuid = bpUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          userUuid = user.uuid,
          syncStatus = DONE,
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          recordedAt = recordedAt,
          deletedAt = deletedAt
      )
    }

    fun createAppointment(patientUuid: UUID, scheduledDate: LocalDate): Appointment {
      return testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          status = Scheduled,
          scheduledDate = scheduledDate)
    }

    // given
    val noBloodSugarsDeletedPatientUuid = UUID.fromString("d05b8ed2-97ae-4fda-8af9-bc4168af3c4d")
    val latestBloodSugarDeletedPatientUuid = UUID.fromString("9e5ec219-f4a5-4bab-9283-0a087c5d7ac2")
    val oldestBloodSugarNotDeletedPatientUuid = UUID.fromString("54e7143c-fe64-4cd8-8c92-f379a79a60f9")
    val allBloodSugarsDeletedPatientUuid = UUID.fromString("05bd9d55-5742-466f-b97e-07301e25fe7e")

    val patients = listOf(
        testData.patientProfile(
            patientUuid = noBloodSugarsDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "No blood sugars are deleted"
        ),
        testData.patientProfile(
            patientUuid = latestBloodSugarDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "Latest blood sugar is deleted"
        ),
        testData.patientProfile(
            patientUuid = oldestBloodSugarNotDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "Oldest blood sugar is not deleted"
        ),
        testData.patientProfile(
            patientUuid = allBloodSugarsDeletedPatientUuid,
            generatePhoneNumber = true,
            patientName = "All blood sugars are deleted"
        )
    )

    patientRepository.save(patients).blockingAwait()

    val bloodSugarForPatientWithNoBloodSugarsDeleted = listOf(
        createBloodSugar(
            bpUuid = UUID.fromString("189b0842-044e-4f1c-a214-24318052f11d"),
            patientUuid = noBloodSugarsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-01T00:00:00Z")
        ),
        createBloodSugar(
            bpUuid = UUID.fromString("ce5deb11-05ee-4f9e-8734-ec3d99f271a9"),
            patientUuid = noBloodSugarsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-02T00:00:00Z")
        )
    )

    val bloodSugarsForPatientWithLatestBloodSugarDeleted = listOf(
        createBloodSugar(
            bpUuid = UUID.fromString("55266e25-0c15-4cd3-969d-3c5d5af48c62"),
            patientUuid = latestBloodSugarDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-03T00:00:00Z")
        ),
        createBloodSugar(
            bpUuid = UUID.fromString("e4c3461e-8624-4b6e-874b-bb73967e423e"),
            patientUuid = latestBloodSugarDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-04T00:00:00Z")
        ),
        createBloodSugar(
            bpUuid = UUID.fromString("e7d19558-36d8-4b5a-a17a-6e3117622b57"),
            patientUuid = latestBloodSugarDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-05T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-05T00:00:00Z")
        )
    )

    val bloodSugarsForPatientWithOldestBloodSugarNotDeleted = listOf(
        createBloodSugar(
            bpUuid = UUID.fromString("1de759ae-9f60-4be5-a1f1-d18143bf8318"),
            patientUuid = oldestBloodSugarNotDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-06T00:00:00Z")
        ),
        createBloodSugar(
            bpUuid = UUID.fromString("f135aaa8-e4d6-48c0-acbf-ed0938c44f34"),
            patientUuid = oldestBloodSugarNotDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-07T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-07T00:00:00Z")
        ),
        createBloodSugar(
            bpUuid = UUID.fromString("44cff8a9-08c2-4a48-9f4b-5c1ec7d9c10c"),
            patientUuid = oldestBloodSugarNotDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-08T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-08T00:00:00Z")
        )
    )

    val bloodSugarsForPatientWithAllBloodSugarsDeleted = listOf(
        createBloodSugar(
            bpUuid = UUID.fromString("264c4295-c61b-41df-8548-460977510574"),
            patientUuid = allBloodSugarsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-09T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-09T00:00:00Z")
        ),
        createBloodSugar(
            bpUuid = UUID.fromString("ff2a665e-d09a-4110-9791-8e966690370f"),
            patientUuid = allBloodSugarsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-10T00:00:00Z"),
            deletedAt = Instant.parse("2018-01-10T00:00:00Z")
        ),
        createBloodSugar(
            bpUuid = UUID.fromString("4e97bd7e-87ea-4d4c-a826-3784703937ed"),
            patientUuid = allBloodSugarsDeletedPatientUuid,
            recordedAt = Instant.parse("2018-01-11T00:00:02Z"),
            deletedAt = Instant.parse("2018-01-11T00:00:00Z")
        )
    )

    bloodSugarRepository
        .save(bloodSugarForPatientWithNoBloodSugarsDeleted + bloodSugarsForPatientWithLatestBloodSugarDeleted + bloodSugarsForPatientWithOldestBloodSugarNotDeleted + bloodSugarsForPatientWithAllBloodSugarsDeleted)
        .blockingAwait()

    val today = LocalDate.now(clock)
    val appointmentsScheduledFor = today.minusDays(1L)

    val appointmentForPatientWithNoBloodSugarDeleted = createAppointment(
        patientUuid = noBloodSugarsDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    val appointmentForPatientWithLatestBloodSugarDeleted = createAppointment(
        patientUuid = latestBloodSugarDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    val appointmentsForPatientWithOldestBloodSugarNotDeleted = createAppointment(
        patientUuid = oldestBloodSugarNotDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    val appointmentsForPatientWithAllBloodSugarsDeleted = createAppointment(
        patientUuid = allBloodSugarsDeletedPatientUuid,
        scheduledDate = appointmentsScheduledFor
    )

    appointmentRepository
        .save(listOf(appointmentForPatientWithNoBloodSugarDeleted, appointmentForPatientWithLatestBloodSugarDeleted, appointmentsForPatientWithOldestBloodSugarNotDeleted, appointmentsForPatientWithAllBloodSugarsDeleted))
        .blockingAwait()

    // when
    val overdueAppointments = appointmentRepository.overdueAppointments(since = today, facility = facility).blockingFirst()
        .associateBy { it.appointment.patientUuid }

    // then
    assertThat(overdueAppointments.keys).containsExactly(noBloodSugarsDeletedPatientUuid, latestBloodSugarDeletedPatientUuid, oldestBloodSugarNotDeletedPatientUuid)

    val appointmentBpUuidOfNoBpsDeletedPatient = overdueAppointments.getValue(noBloodSugarsDeletedPatientUuid).patientLastSeen
    val appointmentBpUuidOfLatestBpDeletedPatient = overdueAppointments.getValue(latestBloodSugarDeletedPatientUuid).patientLastSeen
    val appointmentBpUuidOfOldestBpDeletedPatient = overdueAppointments.getValue(oldestBloodSugarNotDeletedPatientUuid).patientLastSeen

    assertThat(appointmentBpUuidOfNoBpsDeletedPatient).isEqualTo(bloodSugarForPatientWithNoBloodSugarsDeleted[1].recordedAt)
    assertThat(appointmentBpUuidOfLatestBpDeletedPatient).isEqualTo(bloodSugarsForPatientWithLatestBloodSugarDeleted[1].recordedAt)
    assertThat(appointmentBpUuidOfOldestBpDeletedPatient).isEqualTo(bloodSugarsForPatientWithOldestBloodSugarNotDeleted[0].recordedAt)
  }

  @Test
  fun when_setting_appointment_reminder_then_reminder_with_correct_date_should_be_set() {
    // given
    val appointmentDate = LocalDate.parse("2018-01-01")
    val appointmentScheduledAtTimestamp = Instant.now(clock)
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentDate,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofHours(24))

    val reminderDate = LocalDate.parse("2018-02-01")

    // when
    appointmentRepository.createReminder(appointmentUuid, reminderDate)

    // then
    val appointmentUpdatedAtTimestamp = Instant.now(clock)
    val updatedAppointment = getAppointmentByUuid(appointmentUuid)
    with(updatedAppointment) {
      assertThat(remindOn).isEqualTo(reminderDate)
      assertThat(agreedToVisit).isNull()
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(createdAt).isEqualTo(appointmentScheduledAtTimestamp)
      assertThat(createdAt).isLessThan(appointmentUpdatedAtTimestamp)
      assertThat(updatedAt).isEqualTo(appointmentUpdatedAtTimestamp)
    }
  }

  @Test
  fun when_marking_appointment_as_agreed_to_visit_reminder_for_a_month_should_be_set() {
    // given
    val appointmentScheduleDate = LocalDate.parse("2018-01-01")
    val appointmentScheduledAtTimestamp = Instant.now(clock)
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentScheduleDate,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofSeconds(1))
    val userClock = TestUserClock(LocalDate.parse("2018-01-31"))

    // when
    appointmentRepository.markAsAgreedToVisit(appointmentUuid, userClock)

    // then
    val appointmentUpdatedAtTimestamp = Instant.parse("2018-01-01T00:00:01Z")
    with(getAppointmentByUuid(appointmentUuid)) {
      assertThat(remindOn).isEqualTo(LocalDate.parse("2018-02-28"))
      assertThat(agreedToVisit).isTrue()
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(createdAt).isEqualTo(appointmentScheduledAtTimestamp)
      assertThat(createdAt).isLessThan(appointmentUpdatedAtTimestamp)
      assertThat(updatedAt).isEqualTo(appointmentUpdatedAtTimestamp)
    }
  }

  @Test
  fun when_removing_appointment_from_list_then_appointment_status_and_cancel_reason_should_be_updated() {
    // given
    val appointmentScheduleDate = LocalDate.parse("2018-01-01")
    val appointmentScheduledTimestamp = Instant.now(clock)
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentScheduleDate,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofDays(1))

    // when
    appointmentRepository.cancelWithReason(appointmentUuid, PatientNotResponding)

    // then
    val updatedAppointment = getAppointmentByUuid(appointmentUuid)
    val appointmentUpdatedAtTimestamp = Instant.now(clock)
    with(updatedAppointment) {
      assertThat(cancelReason).isEqualTo(PatientNotResponding)
      assertThat(status).isEqualTo(Cancelled)
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(createdAt).isEqualTo(appointmentScheduledTimestamp)
      assertThat(createdAt).isLessThan(appointmentUpdatedAtTimestamp)
      assertThat(updatedAt).isEqualTo(appointmentUpdatedAtTimestamp)
    }
  }

  @Test
  fun when_removing_appointment_with_reason_as_patient_already_visited_then_appointment_should_be_marked_as_visited() {
    // given
    val appointmentScheduleDate = LocalDate.parse("2018-01-01")
    val appointmentScheduledTimestamp = Instant.now(clock)
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentScheduleDate,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofDays(1))

    // when
    appointmentRepository.markAsAlreadyVisited(appointmentUuid)

    // then
    val appointmentUpdatedAtTimestamp = Instant.now(clock)
    with(getAppointmentByUuid(appointmentUuid)) {
      assertThat(cancelReason).isNull()
      assertThat(status).isEqualTo(Visited)
      assertThat(createdAt).isEqualTo(appointmentScheduledTimestamp)
      assertThat(createdAt).isLessThan(appointmentUpdatedAtTimestamp)
      assertThat(updatedAt).isEqualTo(appointmentUpdatedAtTimestamp)
    }
  }

  @Test
  fun high_risk_patients_should_be_present_at_the_top_when_loading_overdue_appointments() {
    data class BP(val systolic: Int, val diastolic: Int)

    fun savePatientAndAppointment(
        patientUuid: UUID,
        appointmentUuid: UUID,
        medicalHistoryUuid: UUID,
        fullName: String,
        bps: List<BP>,
        hasHadHeartAttack: Answer = No,
        hasHadStroke: Answer = No,
        hasDiabetes: Answer = No,
        hasHadKidneyDisease: Answer = No,
        appointmentHasBeenOverdueFor: Duration
    ) {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          patientName = fullName,
          generatePhoneNumber = true,
          generateBusinessId = false
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val scheduledDate = (LocalDateTime.now(clock) - appointmentHasBeenOverdueFor).toLocalDate()
      appointmentRepository.schedule(
          patientUuid = patientUuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = scheduledDate,
          appointmentType = Manual,
          appointmentFacilityUuid = facility.uuid,
          creationFacilityUuid = facility.uuid
      )

      val bloodPressureMeasurements = bps.mapIndexed { index, (systolic, diastolic) ->

        val bpTimestamp = Instant.now(clock).plusSeconds(index.toLong() + 1)

        testData.bloodPressureMeasurement(
            patientUuid = patientUuid,
            systolic = systolic,
            diastolic = diastolic,
            userUuid = user.uuid,
            facilityUuid = facility.uuid,
            recordedAt = bpTimestamp,
            createdAt = bpTimestamp,
            updatedAt = bpTimestamp
        )
      }
      bpRepository.save(bloodPressureMeasurements).blockingAwait()

      medicalHistoryRepository.save(
          uuid = medicalHistoryUuid,
          patientUuid = patientUuid,
          historyEntry = OngoingMedicalHistoryEntry(
              hasHadHeartAttack = hasHadHeartAttack,
              hasHadStroke = hasHadStroke,
              hasHadKidneyDisease = hasHadKidneyDisease,
              hasDiabetes = hasDiabetes
          )
      ).blockingAwait()
      clock.advanceBy(Duration.ofSeconds(bps.size.toLong() + 1))
    }

    // given
    val thirtyDays = Duration.ofDays(30)
    val threeFiftyDays = Duration.ofDays(350)

    savePatientAndAppointment(
        patientUuid = UUID.fromString("0620c310-0248-4d05-b7c4-8134bd7335e8"),
        appointmentUuid = UUID.fromString("efbd510b-e56d-4a1e-94e3-6dd0bbc483e8"),
        medicalHistoryUuid = UUID.fromString("345db2fe-8580-44eb-9eba-6eb69115aab3"),
        fullName = "Has had a heart attack, sBP < 140 & dBP < 110, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadHeartAttack = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("fb4b1804-335b-41ba-b14d-0263ca9cfe6b"),
        appointmentUuid = UUID.fromString("c5ec5523-01f6-4957-832a-2e1fd6be2c85"),
        medicalHistoryUuid = UUID.fromString("3c47287c-a595-4dcf-80cf-317764a916b0"),
        fullName = "Has had a heart attack, sBP > 140, overdue == 30 days",
        bps = listOf(BP(systolic = 145, diastolic = 90)),
        hasHadHeartAttack = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("c99e9290-c456-4944-9ea3-7f68d1da17df"),
        appointmentUuid = UUID.fromString("f8266b57-02d8-4f46-8dda-e8e5313bfa90"),
        medicalHistoryUuid = UUID.fromString("75b9ae47-1986-4b36-85a2-63ce71b47688"),
        fullName = "Has had a heart attack, dBP > 110, overdue == 30 days",
        bps = listOf(BP(systolic = 130, diastolic = 120)),
        hasHadHeartAttack = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("c4c9b12e-05f2-4343-9a1d-319049df4ff7"),
        appointmentUuid = UUID.fromString("1a6265f3-6e7c-4598-afed-5c721d1b489f"),
        medicalHistoryUuid = UUID.fromString("92123194-21d1-42de-a603-c35a7e1a3c75"),
        fullName = "Has had a stroke, sBP < 140 & dBP < 110, overdue == 20 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = Yes,
        appointmentHasBeenOverdueFor = Duration.ofDays(20)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("2c24c3a5-c385-4e5e-8643-b48ab28107c8"),
        appointmentUuid = UUID.fromString("6a5bd240-8154-4cc1-a6d3-5b647f46c397"),
        medicalHistoryUuid = UUID.fromString("a963f0b2-f745-4d0c-97ea-3bccf6d99864"),
        fullName = "Has had a kidney disease, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadKidneyDisease = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("8b497bb5-f809-434c-b1a4-4efdf810f044"),
        appointmentUuid = UUID.fromString("a684baf9-b5b5-4489-ac7d-d3dca698645d"),
        medicalHistoryUuid = UUID.fromString("4c4d6bd4-afef-4989-9b4f-9ef44302c3d0"),
        fullName = "Has diabetes, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasDiabetes = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("cd78a254-a028-4b5d-bdcd-5ff367ad4143"),
        appointmentUuid = UUID.fromString("486f1c48-45df-4eb7-9dd6-dd7d617d293a"),
        medicalHistoryUuid = UUID.fromString("2b12c886-d933-4f36-92da-cf633ae33ea6"),
        fullName = "Has had a heart attack, stroke, kidney disease and has diabetes, sBP > 140, overdue == 30 days",
        bps = listOf(BP(systolic = 140, diastolic = 90)),
        hasHadHeartAttack = Yes,
        hasHadStroke = Yes,
        hasDiabetes = Yes,
        hasHadKidneyDisease = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("9f51709f-b356-4d9c-b6b7-1466eca35b78"),
        appointmentUuid = UUID.fromString("3267bb23-da03-46ef-a09c-89392a444d03"),
        medicalHistoryUuid = UUID.fromString("df353013-ff82-492e-a0ea-2cf7520d9dc0"),
        fullName = "Systolic > 180, overdue == 4 days",
        bps = listOf(BP(systolic = 9000, diastolic = 100)),
        appointmentHasBeenOverdueFor = Duration.ofDays(4)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("a4ed131e-c02e-469d-87d1-8aa63a9da780"),
        appointmentUuid = UUID.fromString("95181e83-a15d-4549-8a12-14d9e9b0c8b8"),
        medicalHistoryUuid = UUID.fromString("0dfd1ab4-5075-4e9f-9708-2ae31b3602e6"),
        fullName = "Diastolic > 110, overdue == 3 days",
        bps = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = Duration.ofDays(3)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("b2a3fbd1-27eb-4ef4-b78d-f66cdbb164b4"),
        appointmentUuid = UUID.fromString("41014c90-aa00-4f9f-bf44-b89ada75f2da"),
        medicalHistoryUuid = UUID.fromString("01d9487e-06f4-4a05-8790-9f2631da19b9"),
        fullName = "Systolic == 180, overdue == 30 days",
        bps = listOf(BP(systolic = 180, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("488ca972-9937-4bce-8ed6-2a926963432a"),
        appointmentUuid = UUID.fromString("a366ae15-5a8f-42ce-b56c-5a0d3826d86d"),
        medicalHistoryUuid = UUID.fromString("18299820-989b-4322-bb90-bdd2815b24f0"),
        fullName = "Systolic == 170, overdue == 30 days",
        bps = listOf(BP(systolic = 170, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays
    )


    savePatientAndAppointment(
        patientUuid = UUID.fromString("a6ca12c1-6f00-4ea9-82f7-b949be415471"),
        appointmentUuid = UUID.fromString("d17f0749-a3a5-4f7a-b6bb-8de83d8ff562"),
        medicalHistoryUuid = UUID.fromString("931b12e2-4c3c-4ec8-86af-c05b96d31ea7"),
        fullName = "Diastolic == 110, overdue == 30 days",
        bps = listOf(BP(systolic = 101, diastolic = 110)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("b261dc0d-c6e1-4a3c-9c48-5335928ddd63"),
        appointmentUuid = UUID.fromString("fbcc6eab-d124-4c92-84db-788d0c905f98"),
        medicalHistoryUuid = UUID.fromString("53629d7d-f181-4236-901d-022f0332ef29"),
        fullName = "Diastolic == 100, overdue == 30 days",
        bps = listOf(BP(systolic = 101, diastolic = 100)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("234cbcb8-c3b1-4b7c-be34-7ac3691c1df7"),
        appointmentUuid = UUID.fromString("2beb3ebc-4bab-40ea-966f-78d904e73ec9"),
        medicalHistoryUuid = UUID.fromString("9517a1ff-7eca-4977-8a6b-b904367750e9"),
        fullName = "BP == 141/91, overdue == 350 days",
        bps = listOf(BP(systolic = 141, diastolic = 91)),
        appointmentHasBeenOverdueFor = threeFiftyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("96c30123-9b08-4e11-b058-e80a62030a31"),
        appointmentUuid = UUID.fromString("1697f7bf-05d5-46ee-9139-11b2b1faf1dd"),
        medicalHistoryUuid = UUID.fromString("b5812690-9493-453f-b357-b456308ed15f"),
        fullName = "BP == 110/80, overdue between 30 days and 1 year",
        bps = listOf(BP(systolic = 110, diastolic = 80)),
        appointmentHasBeenOverdueFor = Duration.ofDays(80)
    )

    // when
    val appointments = appointmentRepository.overdueAppointments(since = LocalDate.now(clock), facility = facility).blockingFirst()

    // then
    assertThat(appointments.map { it.fullName to it.isAtHighRisk }).isEqualTo(listOf(
        "Diastolic > 110, overdue == 3 days" to true,
        "Systolic > 180, overdue == 4 days" to true,
        "Has had a heart attack, sBP > 140, overdue == 30 days" to true,
        "Has had a heart attack, dBP > 110, overdue == 30 days" to true,
        "Has had a heart attack, stroke, kidney disease and has diabetes, sBP > 140, overdue == 30 days" to true,
        "Systolic == 180, overdue == 30 days" to true,
        "Diastolic == 110, overdue == 30 days" to true,
        "Has had a stroke, sBP < 140 & dBP < 110, overdue == 20 days" to false,
        "Has had a heart attack, sBP < 140 & dBP < 110, overdue == 30 days" to false,
        "Has had a kidney disease, overdue == 30 days" to false,
        "Has diabetes, overdue == 30 days" to false,
        "Systolic == 170, overdue == 30 days" to false,
        "Diastolic == 100, overdue == 30 days" to false,
        "BP == 110/80, overdue between 30 days and 1 year" to false,
        "BP == 141/91, overdue == 350 days" to false
    ))
  }

  @Test
  fun when_fetching_overdue_appointments_it_should_exclude_appointments_more_than_a_year_overdue() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        scheduledDate: LocalDate,
        facilityUuid: UUID
    ) {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp)).blockingAwait()

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment)).blockingAwait()
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("9b794e72-6ebb-48c3-a8d7-69751ffeecc2")
    val patientWithTenDaysOverdue = UUID.fromString("0fc57e45-7018-4c03-9218-f90f6fc0f268")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("51467803-f588-4a65-8def-7a15f41bdd13")

    val now = LocalDate.now(clock)
    val facilityUuid = UUID.fromString("ccc66ec1-5029-455b-bf92-caa6d90a9a79")

    createOverdueAppointment(patientWithOneDayOverdue, now.minusDays(1), facilityUuid)
    createOverdueAppointment(patientWithTenDaysOverdue, now.minusDays(10), facilityUuid)
    createOverdueAppointment(patientWithOverAnYearDaysOverdue, now.minusDays(370), facilityUuid)

    //when
    val overduePatientUuids = appointmentRepository.overdueAppointments(since = now, facility = testData.facility(uuid = facilityUuid)).blockingFirst().map { it.appointment.patientUuid }

    //then
    assertThat(overduePatientUuids).containsExactly(patientWithOneDayOverdue, patientWithTenDaysOverdue)
    assertThat(overduePatientUuids).doesNotContain(patientWithOverAnYearDaysOverdue)
  }

  @Test
  fun when_fetching_appointment_for_patient_it_should_return_the_last_created_appointment() {
    // given
    val scheduledDateForFirstAppointment = LocalDate.parse("2018-02-01")
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = UUID.fromString("faa8cd6c-4aca-41c9-983a-1a10b6704466"),
        appointmentDate = scheduledDateForFirstAppointment,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )

    clock.advanceBy(Duration.ofDays(1))

    val scheduledDateForSecondAppointment = LocalDate.parse("2018-02-08")
    val secondAppointment = appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = UUID.fromString("634b4807-d3a8-42a9-8411-7c921ed57f49"),
        appointmentDate = scheduledDateForSecondAppointment,
        appointmentType = Manual,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )

    // when
    val appointment = appointmentRepository.lastCreatedAppointmentForPatient(patientUuid).toNullable()!!

    // then
    assertThat(appointment).isEqualTo(secondAppointment)
  }

  @Test
  fun marking_appointment_older_than_current_date_as_visited_should_work_correctly() {
    // given
    val firstAppointmentUuid = UUID.fromString("96b21ba5-e12d-41ec-bfc9-f09bac6ed435")
    val secondAppointmentUuid = UUID.fromString("2fbbf320-3b78-4f26-a8d6-5a90d2800711")

    val appointmentScheduleDate = LocalDate.parse("2018-02-01")

    clock.advanceBy(Duration.ofHours(1))
    database
        .appointmentDao()
        .save(listOf(testData.appointment(
            uuid = firstAppointmentUuid,
            patientUuid = patientUuid,
            status = Scheduled,
            syncStatus = DONE,
            scheduledDate = appointmentScheduleDate,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock)
        )))
    val firstAppointmentBeforeMarkingAsCreatedOnCurrentDay = getAppointmentByUuid(firstAppointmentUuid)

    // then
    clock.advanceBy(Duration.ofHours(1))
    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid).blockingAwait()
    assertThat(getAppointmentByUuid(firstAppointmentUuid)).isEqualTo(firstAppointmentBeforeMarkingAsCreatedOnCurrentDay)

    // then
    clock.advanceBy(Duration.ofDays(1))
    database
        .appointmentDao()
        .save(listOf(testData.appointment(
            uuid = secondAppointmentUuid,
            patientUuid = patientUuid,
            scheduledDate = appointmentScheduleDate,
            status = Scheduled,
            syncStatus = PENDING,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock)
        )))

    val secondAppointmentBeforeMarkingAsCreatedOnNextDay = getAppointmentByUuid(secondAppointmentUuid)
    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid).blockingAwait()

    val firstAppointmentAfterMarkingAsCreatedOnNextDay = getAppointmentByUuid(firstAppointmentUuid)

    with(firstAppointmentAfterMarkingAsCreatedOnNextDay) {
      assertThat(status).isEqualTo(Visited)
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(createdAt).isEqualTo(firstAppointmentBeforeMarkingAsCreatedOnCurrentDay.createdAt)
      assertThat(createdAt).isLessThan(updatedAt)
      assertThat(updatedAt).isEqualTo(Instant.now(clock))
    }
    assertThat(getAppointmentByUuid(secondAppointmentUuid))
        .isEqualTo(secondAppointmentBeforeMarkingAsCreatedOnNextDay)
  }

  @Test
  fun when_scheduling_appointment_for_defaulter_patient_then_the_appointment_should_be_saved_as_defaulter() {
    // given
    val appointmentScheduleDate = LocalDate.parse("2018-01-01")

    // when
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentScheduleDate,
        appointmentType = Automatic,
        appointmentFacilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid
    )

    // then
    val savedAppointment = getAppointmentByUuid(appointmentUuid)
    with(savedAppointment) {
      assertThat(patientUuid).isEqualTo(this@AppointmentRepositoryAndroidTest.patientUuid)
      assertThat(scheduledDate).isEqualTo(appointmentScheduleDate)
      assertThat(status).isEqualTo(Scheduled)
      assertThat(syncStatus).isEqualTo(PENDING)
      assertThat(appointmentType).isEqualTo(Automatic)
    }
  }

  @Test
  fun when_picking_overdue_appointment_then_the_latest_recorded_bp_should_be_considered() {
    fun createBloodPressure(patientProfile: PatientProfile, recordedAt: Instant): BloodPressureMeasurement {
      return testData.bloodPressureMeasurement(
          patientUuid = patientProfile.patient.uuid,
          recordedAt = recordedAt
      )
    }

    fun scheduleAppointment(
        appointmentUuid: UUID,
        patientProfile: PatientProfile
    ): Single<Appointment> {
      return Single.just(appointmentRepository.schedule(
          patientUuid = patientProfile.patient.uuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = LocalDate.parse("2017-12-30"),
          appointmentType = Manual,
          appointmentFacilityUuid = facility.uuid,
          creationFacilityUuid = facility.uuid
      ))
    }

    // given
    val firstPatient = testData.patientProfile(
        patientUuid = UUID.fromString("e1943cfb-faf0-42c4-b5b6-14b5153295b2"),
        generatePhoneNumber = true
    )
    val secondPatient = testData.patientProfile(
        patientUuid = UUID.fromString("08c7acbf-61f1-439e-93a8-43ba4e990428"),
        generatePhoneNumber = true
    )

    patientRepository.save(listOf(firstPatient, secondPatient)).blockingAwait()

    val earlierRecordedBpForFirstPatient = createBloodPressure(
        patientProfile = firstPatient,
        recordedAt = Instant.parse("2017-12-31T23:59:59Z")
    )
    val laterRecordedBpForFirstPatient = createBloodPressure(
        patientProfile = firstPatient,
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    val earlierRecordedBpForSecondPatient = createBloodPressure(
        patientProfile = secondPatient,
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val laterRecordedBpForSecondPatient = createBloodPressure(
        patientProfile = secondPatient,
        recordedAt = Instant.parse("2018-01-01T00:00:01Z")
    )

    bpRepository.save(listOf(laterRecordedBpForFirstPatient, earlierRecordedBpForFirstPatient, earlierRecordedBpForSecondPatient, laterRecordedBpForSecondPatient)).blockingAwait()

    val appointmentUuidForFirstPatient = UUID.fromString("d9fd734d-13b8-43e3-a2d7-b40341699050")
    val appointmentUuidForSecondPatient = UUID.fromString("979e4a13-ae73-4dcf-a1e0-31465dff5512")

    scheduleAppointment(appointmentUuidForFirstPatient, firstPatient).blockingGet()
    scheduleAppointment(appointmentUuidForSecondPatient, secondPatient).blockingGet()

    // when
    val bloodPressuresByAppointmentUuid = appointmentRepository
        .overdueAppointments(since = LocalDate.now(clock), facility = facility)
        .blockingFirst()
        .associateBy({ it.appointment.uuid }, { it.patientLastSeen })

    // then
    val expected = mapOf(
        appointmentUuidForFirstPatient to laterRecordedBpForFirstPatient.recordedAt,
        appointmentUuidForSecondPatient to laterRecordedBpForSecondPatient.recordedAt
    )
    assertThat(bloodPressuresByAppointmentUuid).isEqualTo(expected)
  }

  @Test
  fun when_picking_overdue_appointment_then_the_latest_recorded_blood_sugar_should_be_considered() {
    fun createBloodSugar(patientProfile: PatientProfile, recordedAt: Instant): BloodSugarMeasurement {
      return testData.bloodSugarMeasurement(
          patientUuid = patientProfile.patient.uuid,
          recordedAt = recordedAt
      )
    }

    fun scheduleAppointment(
        appointmentUuid: UUID,
        patientProfile: PatientProfile
    ): Single<Appointment> {
      return Single.just(appointmentRepository.schedule(
          patientUuid = patientProfile.patient.uuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = LocalDate.parse("2017-12-30"),
          appointmentType = Manual,
          appointmentFacilityUuid = facility.uuid,
          creationFacilityUuid = facility.uuid
      ))
    }

    // given
    val firstPatient = testData.patientProfile(
        patientUuid = UUID.fromString("e1943cfb-faf0-42c4-b5b6-14b5153295b2"),
        generatePhoneNumber = true
    )
    val secondPatient = testData.patientProfile(
        patientUuid = UUID.fromString("08c7acbf-61f1-439e-93a8-43ba4e990428"),
        generatePhoneNumber = true
    )

    patientRepository.save(listOf(firstPatient, secondPatient)).blockingAwait()

    val earlierRecordedBloodSugarForFirstPatient = createBloodSugar(
        patientProfile = firstPatient,
        recordedAt = Instant.parse("2017-12-31T23:59:59Z")
    )
    val laterRecordedBloodSugarForFirstPatient = createBloodSugar(
        patientProfile = firstPatient,
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    val earlierRecordedBloodSugarForSecondPatient = createBloodSugar(
        patientProfile = secondPatient,
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val laterRecordedBloodSugarForSecondPatient = createBloodSugar(
        patientProfile = secondPatient,
        recordedAt = Instant.parse("2018-01-01T00:00:01Z")
    )

    bloodSugarRepository.save(listOf(laterRecordedBloodSugarForFirstPatient, earlierRecordedBloodSugarForFirstPatient, earlierRecordedBloodSugarForSecondPatient, laterRecordedBloodSugarForSecondPatient)).blockingAwait()

    val appointmentUuidForFirstPatient = UUID.fromString("d9fd734d-13b8-43e3-a2d7-b40341699050")
    val appointmentUuidForSecondPatient = UUID.fromString("979e4a13-ae73-4dcf-a1e0-31465dff5512")

    scheduleAppointment(appointmentUuidForFirstPatient, firstPatient).blockingGet()
    scheduleAppointment(appointmentUuidForSecondPatient, secondPatient).blockingGet()

    // when
    val bloodSugarByAppointmentUuid = appointmentRepository
        .overdueAppointments(since = LocalDate.now(clock), facility = facility)
        .blockingFirst()
        .associateBy({ it.appointment.uuid }, { it.patientLastSeen })

    // then
    val expected = mapOf(
        appointmentUuidForFirstPatient to laterRecordedBloodSugarForFirstPatient.recordedAt,
        appointmentUuidForSecondPatient to laterRecordedBloodSugarForSecondPatient.recordedAt
    )
    assertThat(bloodSugarByAppointmentUuid).isEqualTo(expected)
  }

  @Test
  fun when_picking_overdue_appointment_and_blood_sugar_is_latest_compared_to_blood_pressure_then_blood_sugar_should_be_considered() {
    fun createBloodSugar(patientProfile: PatientProfile, recordedAt: Instant): BloodSugarMeasurement {
      return testData.bloodSugarMeasurement(
          patientUuid = patientProfile.patient.uuid,
          recordedAt = recordedAt
      )
    }

    fun createBloodPressure(patientProfile: PatientProfile, recordedAt: Instant): BloodPressureMeasurement {
      return testData.bloodPressureMeasurement(
          patientUuid = patientProfile.patient.uuid,
          recordedAt = recordedAt
      )
    }

    fun scheduleAppointment(
        appointmentUuid: UUID,
        patientProfile: PatientProfile
    ): Single<Appointment> {
      return Single.just(appointmentRepository.schedule(
          patientUuid = patientProfile.patient.uuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = LocalDate.parse("2017-12-30"),
          appointmentType = Manual,
          appointmentFacilityUuid = facility.uuid,
          creationFacilityUuid = facility.uuid
      ))
    }

    // given
    val patient = testData.patientProfile(
        patientUuid = UUID.fromString("e1943cfb-faf0-42c4-b5b6-14b5153295b2"),
        generatePhoneNumber = true
    )

    patientRepository.save(listOf(patient)).blockingAwait()

    val earlierRecordedBPForPatient = createBloodPressure(
        patientProfile = patient,
        recordedAt = Instant.parse("2017-12-31T23:59:59Z")
    )
    val laterRecordedBloodSugarForPatient = createBloodSugar(
        patientProfile = patient,
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    bpRepository.save(listOf(earlierRecordedBPForPatient))
    bloodSugarRepository.save(listOf(laterRecordedBloodSugarForPatient)).blockingAwait()

    val appointmentUuidForFirstPatient = UUID.fromString("d9fd734d-13b8-43e3-a2d7-b40341699050")

    scheduleAppointment(appointmentUuidForFirstPatient, patient).blockingGet()
    scheduleAppointment(appointmentUuidForFirstPatient, patient).blockingGet()

    // when
    val bloodSugarByAppointmentUuid = appointmentRepository
        .overdueAppointments(since = LocalDate.now(clock), facility = facility)
        .blockingFirst()
        .associateBy({ it.appointment.uuid }, { it.patientLastSeen })

    // then
    val expected = mapOf(
        appointmentUuidForFirstPatient to laterRecordedBloodSugarForPatient.recordedAt
    )
    assertThat(bloodSugarByAppointmentUuid).isEqualTo(expected)
  }

  @Test
  fun when_picking_overdue_appointment_and_blood_pressure_is_latest_compared_to_blood_sugar_then_blood_pressure_should_be_considered() {
    fun createBloodSugar(patientProfile: PatientProfile, recordedAt: Instant): BloodSugarMeasurement {
      return testData.bloodSugarMeasurement(
          patientUuid = patientProfile.patient.uuid,
          recordedAt = recordedAt
      )
    }

    fun createBloodPressure(patientProfile: PatientProfile, recordedAt: Instant): BloodPressureMeasurement {
      return testData.bloodPressureMeasurement(
          patientUuid = patientProfile.patient.uuid,
          recordedAt = recordedAt
      )
    }

    fun scheduleAppointment(
        appointmentUuid: UUID,
        patientProfile: PatientProfile
    ): Single<Appointment> {
      return Single.just(appointmentRepository.schedule(
          patientUuid = patientProfile.patient.uuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = LocalDate.parse("2017-12-30"),
          appointmentType = Manual,
          appointmentFacilityUuid = facility.uuid,
          creationFacilityUuid = facility.uuid
      ))
    }

    // given
    val patient = testData.patientProfile(
        patientUuid = UUID.fromString("e1943cfb-faf0-42c4-b5b6-14b5153295b2"),
        generatePhoneNumber = true
    )

    patientRepository.save(listOf(patient)).blockingAwait()

    val earlierRecordedBloodSugarForPatient = createBloodSugar(
        patientProfile = patient,
        recordedAt = Instant.parse("2017-12-31T23:59:59Z")
    )
    val laterRecordedBPForPatient = createBloodPressure(
        patientProfile = patient,
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    bloodSugarRepository.save(listOf(earlierRecordedBloodSugarForPatient))
    bpRepository.save(listOf(laterRecordedBPForPatient)).blockingAwait()

    val appointmentUuidForFirstPatient = UUID.fromString("d9fd734d-13b8-43e3-a2d7-b40341699050")

    scheduleAppointment(appointmentUuidForFirstPatient, patient).blockingGet()
    scheduleAppointment(appointmentUuidForFirstPatient, patient).blockingGet()

    // when
    val bloodSugarByAppointmentUuid = appointmentRepository
        .overdueAppointments(since = LocalDate.now(clock), facility = facility)
        .blockingFirst()
        .associateBy({ it.appointment.uuid }, { it.patientLastSeen })

    // then
    val expected = mapOf(
        appointmentUuidForFirstPatient to laterRecordedBPForPatient.recordedAt
    )
    assertThat(bloodSugarByAppointmentUuid).isEqualTo(expected)
  }

  @Test
  fun deleted_patients_must_be_excluded_when_loading_overdue_appointments() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        facilityUuid: UUID,
        isPatientDeleted: Boolean
    ) {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientDeletedAt = if (isPatientDeleted) Instant.now() else null
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp)).blockingAwait()

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = LocalDate.now(clock).minusDays(1),
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment)).blockingAwait()
    }

    //given
    val deletedPatientId = UUID.fromString("97d05796-614c-46de-a10a-e12cf595f4ff")
    createOverdueAppointment(
        patientUuid = deletedPatientId,
        facilityUuid = facility.uuid,
        isPatientDeleted = true
    )
    val notDeletedPatientId = UUID.fromString("4e642ef2-1991-42ae-ba61-a10809c78f5d")
    createOverdueAppointment(
        patientUuid = notDeletedPatientId,
        facilityUuid = facility.uuid,
        isPatientDeleted = false
    )

    // when
    val overdueAppointments = appointmentRepository.overdueAppointments(since = LocalDate.now(clock), facility = facility).blockingFirst()

    //then
    assertThat(overdueAppointments).hasSize(1)
    assertThat(overdueAppointments.first().appointment.patientUuid).isEqualTo(notDeletedPatientId)
  }

  @Test
  fun deleted_appointments_must_be_excluded_when_loading_overdue_appointments() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        facilityUuid: UUID,
        isAppointmentDeleted: Boolean
    ) {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp)).blockingAwait()

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = LocalDate.now(clock).minusDays(1),
          status = Scheduled,
          cancelReason = null,
          deletedAt = if (isAppointmentDeleted) Instant.now() else null
      )
      appointmentRepository.save(listOf(appointment)).blockingAwait()
    }

    //given
    val patientIdWithDeletedAppointment = UUID.fromString("97d05796-614c-46de-a10a-e12cf595f4ff")
    createOverdueAppointment(
        patientUuid = patientIdWithDeletedAppointment,
        facilityUuid = facility.uuid,
        isAppointmentDeleted = true
    )
    val patientIdWithoutDeletedAppointment = UUID.fromString("4e642ef2-1991-42ae-ba61-a10809c78f5d")
    createOverdueAppointment(
        patientUuid = patientIdWithoutDeletedAppointment,
        facilityUuid = facility.uuid,
        isAppointmentDeleted = false
    )

    // when
    val overdueAppointments = appointmentRepository.overdueAppointments(since = LocalDate.now(clock), facility = facility).blockingFirst()

    //then
    assertThat(overdueAppointments).hasSize(1)
    assertThat(overdueAppointments.first().appointment.patientUuid).isEqualTo(patientIdWithoutDeletedAppointment)
  }

  @Test
  fun appointments_that_are_still_scheduled_after_the_schedule_date_should_be_fetched_as_overdue_appointments() {

    fun createAppointmentRecord(
        patientUuid: UUID,
        bpUuid: UUID,
        appointmentUuid: UUID,
        scheduleAppointmentOn: LocalDate
    ): RecordAppointment {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true
      )

      val bloodPressureMeasurement = testData.bloodPressureMeasurement(
          uuid = bpUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          userUuid = user.uuid,
          systolic = 120,
          diastolic = 80,
          recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
          deletedAt = null
      )

      val appointment = testData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          scheduledDate = scheduleAppointmentOn,
          status = Scheduled,
          cancelReason = null,
          remindOn = null,
          agreedToVisit = null
      )

      return RecordAppointment(patientProfile, bloodPressureMeasurement, null, appointment)
    }

    // given
    val currentDate = LocalDate.parse("2018-01-05")

    val oneWeekBeforeCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("c5bb0ab7-516d-4c61-ac36-b806ba9bcca5"),
        bpUuid = UUID.fromString("85765883-b964-4322-a7e3-c922612b078d"),
        appointmentUuid = UUID.fromString("064a0417-d485-40f0-9659-dbb7a4efbfb7"),
        scheduleAppointmentOn = currentDate.minusDays(7)
    )

    val oneDayBeforeCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("7dd6a3c6-2977-45d9-bf22-f2b8929d227e"),
        bpUuid = UUID.fromString("e27345b6-0463-410d-b433-ada8adf8f6f7"),
        appointmentUuid = UUID.fromString("899a7269-01b0-4d59-9e3b-5a6cc82985d2"),
        scheduleAppointmentOn = currentDate.minusDays(1)
    )

    val onCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("c83a03f3-61b4-4af6-bcbf-3094ed4044a1"),
        bpUuid = UUID.fromString("2f439af6-bff9-4d85-9179-9697171863fb"),
        appointmentUuid = UUID.fromString("6419fb68-6e1d-4928-8435-777da07c54d9"),
        scheduleAppointmentOn = currentDate
    )

    val afterCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("fae12ba1-e958-4aaf-9802-0b4b09535469"),
        bpUuid = UUID.fromString("4b25b6bb-4279-4816-81a6-f5f325c832d4"),
        appointmentUuid = UUID.fromString("b422ca39-2090-4c24-9a4c-5a8904403a57"),
        scheduleAppointmentOn = currentDate.plusDays(1)
    )

    listOf(oneWeekBeforeCurrentDate, oneDayBeforeCurrentDate, onCurrentDate, afterCurrentDate)
        .forEach { it.save(patientRepository, bpRepository, bloodSugarRepository, appointmentRepository) }

    // when
    val overdueAppointments = appointmentRepository
        .overdueAppointments(since = currentDate, facility = facility)
        .blockingFirst()

    // then
    val expectedAppointments = listOf(oneWeekBeforeCurrentDate, oneDayBeforeCurrentDate).map { it.toOverdueAppointment(facility.name) }

    assertThat(overdueAppointments).containsExactlyElementsIn(expectedAppointments)
  }

  @Test
  fun appointments_with_reminder_dates_before_the_current_date_should_be_shown_when_fetching_overdue_appointments() {

    val currentDate = LocalDate.parse("2018-01-05")

    fun createAppointmentRecord(
        patientUuid: UUID,
        bpUuid: UUID,
        appointmentUuid: UUID,
        appointmentReminderOn: LocalDate
    ): RecordAppointment {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true
      )

      val bloodPressureMeasurement = testData.bloodPressureMeasurement(
          uuid = bpUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          userUuid = user.uuid,
          systolic = 120,
          diastolic = 80,
          recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
          deletedAt = null
      )

      val appointment = testData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          scheduledDate = currentDate.minusWeeks(2),
          status = Scheduled,
          cancelReason = null,
          remindOn = appointmentReminderOn,
          agreedToVisit = null
      )

      return RecordAppointment(patientProfile, bloodPressureMeasurement, null, appointment)
    }

    // given
    val remindOneWeekBeforeCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("417c19d3-68a0-4936-bc4f-5b7c2a73ccc7"),
        bpUuid = UUID.fromString("3414fd9a-8b30-4850-9f8f-3de9305dcb6c"),
        appointmentUuid = UUID.fromString("053f2f73-b693-420c-a9c6-d8aae1c77395"),
        appointmentReminderOn = currentDate.minusWeeks(1)
    )

    val remindOneDayBeforeCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("0af5c909-551b-448d-988e-b00b3304f738"),
        bpUuid = UUID.fromString("6b5aed42-9e78-486a-bee2-392455993dfe"),
        appointmentUuid = UUID.fromString("e58cfd76-aaeb-42a8-8bf1-4c71614c6288"),
        appointmentReminderOn = currentDate.minusDays(1)
    )

    val remindOnCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("6fc5a658-4afc-473b-a062-c57849f4ade9"),
        bpUuid = UUID.fromString("ff440058-6dbc-4283-b0c3-882ee069ed6c"),
        appointmentUuid = UUID.fromString("27625873-bda2-47ff-ab2c-a664224a8d7e"),
        appointmentReminderOn = currentDate
    )

    val remindAfterCurrentDate = createAppointmentRecord(
        patientUuid = UUID.fromString("f1ddc613-a7ca-4bb4-a1a0-233672a4eb1d"),
        bpUuid = UUID.fromString("6847f8ed-8868-42a1-b962-f5b4258f224c"),
        appointmentUuid = UUID.fromString("2000fdda-8e42-4067-b7d3-38cb9e74f88b"),
        appointmentReminderOn = currentDate.plusDays(1)
    )

    listOf(remindOneWeekBeforeCurrentDate, remindOneDayBeforeCurrentDate, remindOnCurrentDate, remindAfterCurrentDate)
        .forEach { it.save(patientRepository, bpRepository, bloodSugarRepository, appointmentRepository) }

    // when
    val overdueAppointments = appointmentRepository
        .overdueAppointments(since = currentDate, facility = facility)
        .blockingFirst()

    // then
    val expectedAppointments = listOf(remindOneWeekBeforeCurrentDate, remindOneDayBeforeCurrentDate).map { it.toOverdueAppointment(facility.name) }

    assertThat(overdueAppointments).containsExactlyElementsIn(expectedAppointments)
  }

  @Test
  fun patients_without_phone_number_should_not_be_shown_when_fetching_overdue_appointments() {

    val currentDate = LocalDate.parse("2018-01-05")

    fun createAppointmentRecord(
        patientUuid: UUID,
        bpUuid: UUID,
        appointmentUuid: UUID,
        patientPhoneNumber: PatientPhoneNumber?
    ): RecordAppointment {
      val patientProfile = with(testData.patientProfile(patientUuid = patientUuid, generatePhoneNumber = false)) {
        val phoneNumbers = if (patientPhoneNumber == null) emptyList() else listOf(patientPhoneNumber.withPatientUuid(patientUuid))

        this.copy(phoneNumbers = phoneNumbers)
      }

      val bloodPressureMeasurement = testData.bloodPressureMeasurement(
          uuid = bpUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          userUuid = user.uuid,
          systolic = 120,
          diastolic = 80,
          recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
          deletedAt = null
      )

      val appointment = testData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          scheduledDate = LocalDate.parse("2018-01-04"),
          status = Scheduled,
          cancelReason = null,
          remindOn = null,
          agreedToVisit = null
      )

      return RecordAppointment(patientProfile, bloodPressureMeasurement, null, appointment)
    }

    // given
    val withPhoneNumber = createAppointmentRecord(
        patientUuid = UUID.fromString("417c19d3-68a0-4936-bc4f-5b7c2a73ccc7"),
        bpUuid = UUID.fromString("3414fd9a-8b30-4850-9f8f-3de9305dcb6c"),
        appointmentUuid = UUID.fromString("053f2f73-b693-420c-a9c6-d8aae1c77395"),
        patientPhoneNumber = testData.patientPhoneNumber()
    )

    val withDeletedPhoneNumber = createAppointmentRecord(
        patientUuid = UUID.fromString("0af5c909-551b-448d-988e-b00b3304f738"),
        bpUuid = UUID.fromString("6b5aed42-9e78-486a-bee2-392455993dfe"),
        appointmentUuid = UUID.fromString("e58cfd76-aaeb-42a8-8bf1-4c71614c6288"),
        patientPhoneNumber = testData.patientPhoneNumber(deletedAt = Instant.parse("2018-01-01T00:00:00Z"))
    )

    val withoutPhoneNumber = createAppointmentRecord(
        patientUuid = UUID.fromString("f1ddc613-a7ca-4bb4-a1a0-233672a4eb1d"),
        bpUuid = UUID.fromString("6847f8ed-8868-42a1-b962-f5b4258f224c"),
        appointmentUuid = UUID.fromString("2000fdda-8e42-4067-b7d3-38cb9e74f88b"),
        patientPhoneNumber = null
    )

    listOf(withPhoneNumber, withDeletedPhoneNumber, withoutPhoneNumber)
        .forEach { it.save(patientRepository, bpRepository, bloodSugarRepository, appointmentRepository) }

    // when
    val overdueAppointments = appointmentRepository
        .overdueAppointments(since = currentDate, facility = facility)
        .blockingFirst()

    // then
    val expectedAppointments = listOf(withPhoneNumber).map { it.toOverdueAppointment(facility.name) }

    assertThat(overdueAppointments).containsExactlyElementsIn(expectedAppointments)
  }

  @Test
  fun patients_without_blood_pressure_should_not_be_shown_when_fetching_overdue_appointments() {

    val currentDate = LocalDate.parse("2018-01-05")

    fun createAppointmentRecord(
        patientUuid: UUID,
        bpUuid: UUID?,
        appointmentUuid: UUID
    ): RecordAppointment {
      val patientProfile = testData.patientProfile(patientUuid = patientUuid, generatePhoneNumber = true)

      val bloodPressureMeasurement = if (bpUuid != null) {
        testData.bloodPressureMeasurement(
            uuid = bpUuid,
            patientUuid = patientUuid,
            facilityUuid = facility.uuid,
            userUuid = user.uuid,
            systolic = 120,
            diastolic = 80,
            recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
            deletedAt = null
        )
      } else null

      val appointment = testData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          scheduledDate = LocalDate.parse("2018-01-04"),
          status = Scheduled,
          cancelReason = null,
          remindOn = null,
          agreedToVisit = null
      )

      return RecordAppointment(patientProfile, bloodPressureMeasurement, null, appointment)
    }

    // given
    val withBloodPressure = createAppointmentRecord(
        patientUuid = UUID.fromString("417c19d3-68a0-4936-bc4f-5b7c2a73ccc7"),
        bpUuid = UUID.fromString("3414fd9a-8b30-4850-9f8f-3de9305dcb6c"),
        appointmentUuid = UUID.fromString("053f2f73-b693-420c-a9c6-d8aae1c77395")
    )

    val withoutBloodPressure = createAppointmentRecord(
        patientUuid = UUID.fromString("0af5c909-551b-448d-988e-b00b3304f738"),
        bpUuid = null,
        appointmentUuid = UUID.fromString("e58cfd76-aaeb-42a8-8bf1-4c71614c6288")
    )

    listOf(withBloodPressure, withoutBloodPressure)
        .forEach { it.save(patientRepository, bpRepository, bloodSugarRepository, appointmentRepository) }

    // when
    val overdueAppointments = appointmentRepository
        .overdueAppointments(since = currentDate, facility = facility)
        .blockingFirst()

    // then
    val expectedAppointments = listOf(withBloodPressure).map { it.toOverdueAppointment(facility.name) }

    assertThat(overdueAppointments).containsExactlyElementsIn(expectedAppointments)
  }

  @Test
  fun patients_without_blood_sugars_should_not_be_shown_when_fetching_overdue_appointments() {

    val currentDate = LocalDate.parse("2018-01-05")

    fun createAppointmentRecord(
        patientUuid: UUID,
        bloodSugarUuid: UUID?,
        appointmentUuid: UUID
    ): RecordAppointment {
      val patientProfile = testData.patientProfile(patientUuid = patientUuid, generatePhoneNumber = true)

      val bloodSugarMeasurement = if (bloodSugarUuid != null) {
        testData.bloodSugarMeasurement(
            uuid = bloodSugarUuid,
            patientUuid = patientUuid,
            facilityUuid = facility.uuid,
            userUuid = user.uuid,
            reading = BloodSugarReading("256", Random),
            recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
            deletedAt = null
        )
      } else null

      val appointment = testData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          scheduledDate = LocalDate.parse("2018-01-04"),
          status = Scheduled,
          cancelReason = null,
          remindOn = null,
          agreedToVisit = null
      )

      return RecordAppointment(patientProfile, null, bloodSugarMeasurement, appointment)
    }

    // given
    val withBloodSugar = createAppointmentRecord(
        patientUuid = UUID.fromString("417c19d3-68a0-4936-bc4f-5b7c2a73ccc7"),
        bloodSugarUuid = UUID.fromString("3414fd9a-8b30-4850-9f8f-3de9305dcb6c"),
        appointmentUuid = UUID.fromString("053f2f73-b693-420c-a9c6-d8aae1c77395")
    )

    val withoutBloodSugar = createAppointmentRecord(
        patientUuid = UUID.fromString("0af5c909-551b-448d-988e-b00b3304f738"),
        bloodSugarUuid = null,
        appointmentUuid = UUID.fromString("e58cfd76-aaeb-42a8-8bf1-4c71614c6288")
    )

    listOf(withBloodSugar, withoutBloodSugar)
        .forEach { it.save(patientRepository, bpRepository, bloodSugarRepository, appointmentRepository) }

    // when
    val overdueAppointments = appointmentRepository
        .overdueAppointments(since = currentDate, facility = facility)
        .blockingFirst()

    // then
    val expectedAppointments = listOf(withBloodSugar).map { it.toOverdueAppointment(facility.name) }

    assertThat(overdueAppointments).containsExactlyElementsIn(expectedAppointments)
  }

  @Test
  fun patients_with_different_combinations_of_blood_pressure_and_blood_sugars_should_be_included_in_overdue_list() {

    data class BP(val systolic: Int, val diastolic: Int)

    fun savePatientBloodPressureAndBloodSugar(
        patientUuid: UUID,
        appointmentUuid: UUID = UUID.randomUUID(),
        fullName: String,
        bps: List<BP> = emptyList(),
        bloodSugars: List<BloodSugarReading> = emptyList(),
        hasHadHeartAttack: Answer = No,
        hasHadStroke: Answer = No,
        hasDiabetes: Answer = No,
        hasHadKidneyDisease: Answer = No,
        appointmentHasBeenOverdueFor: Duration
    ) {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          patientName = fullName,
          generatePhoneNumber = true,
          generateBusinessId = false
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val scheduledDate = (LocalDateTime.now(clock) - appointmentHasBeenOverdueFor).toLocalDate()
      appointmentRepository.schedule(
          patientUuid = patientUuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = scheduledDate,
          appointmentType = Manual,
          appointmentFacilityUuid = facility.uuid,
          creationFacilityUuid = facility.uuid
      )

      val bloodPressureMeasurements = bps.mapIndexed { index, (systolic, diastolic) ->

        clock.advanceBy(Duration.ofSeconds(1L))
        val bpTimestamp = Instant.now(clock)

        testData.bloodPressureMeasurement(
            patientUuid = patientUuid,
            systolic = systolic,
            diastolic = diastolic,
            userUuid = user.uuid,
            facilityUuid = facility.uuid,
            recordedAt = bpTimestamp,
            createdAt = bpTimestamp,
            updatedAt = bpTimestamp
        )
      }
      bpRepository.save(bloodPressureMeasurements).blockingAwait()

      val bloodSugarMeasurements = bloodSugars.mapIndexed { index, bloodSugarReading ->

        clock.advanceBy(Duration.ofSeconds(1L))
        val bloodSugarTimestamp = Instant.now(clock)

        testData.bloodSugarMeasurement(
            patientUuid = patientUuid,
            reading = bloodSugarReading,
            userUuid = user.uuid,
            facilityUuid = facility.uuid,
            recordedAt = bloodSugarTimestamp,
            createdAt = bloodSugarTimestamp,
            updatedAt = bloodSugarTimestamp
        )
      }
      bloodSugarRepository.save(bloodSugarMeasurements).blockingAwait()

      medicalHistoryRepository.save(
          uuid = UUID.fromString("29a124c9-b6d4-4faa-91a8-a294f848c912"),
          patientUuid = patientUuid,
          historyEntry = OngoingMedicalHistoryEntry(
              hasHadHeartAttack = hasHadHeartAttack,
              hasHadStroke = hasHadStroke,
              hasHadKidneyDisease = hasHadKidneyDisease,
              hasDiabetes = hasDiabetes
          )
      ).blockingAwait()
    }

    // when
    val thirtyDays = Duration.ofDays(30)

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("b408f97b-0c1c-41ac-b63d-2d6f5811d22c"),
        fullName = "Diastolic > 110, overdue == 3 days",
        bps = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = Duration.ofDays(3)
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("d815ca26-b5f5-488e-a083-1946556993c5"),
        fullName = "FBS = 199, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("199", Fasting)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("21d54d0e-dba1-4823-9b71-1d428a8e1a19"),
        fullName = "FBS = 200, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("200", Fasting)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("1da72cec-fa4a-4d7b-aec8-cefdbae51f90"),
        fullName = "FBS = 201, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("201", Fasting)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("4bd709ce-5fd3-47b2-95d3-2796b8b12916"),
        fullName = "RBS = 299, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("299", Random)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("627f96dd-b8f6-428e-a68b-55f2b09447ef"),
        fullName = "RBS = 300, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("300", Random)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("e66a1cf7-5970-426c-a87c-3402c832400a"),
        fullName = "RBS = 301, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("301", Random)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("172b95be-075c-42ca-82bb-78e743250457"),
        fullName = "Latest RBS = 200, overdue = 30 days",
        bloodSugars = listOf(
            BloodSugarReading("301", Random),
            BloodSugarReading("200", Random)
        ),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("9f84c79a-0b27-4aeb-a8fa-bd32df370592"),
        fullName = "BP = 120/80, RBS = 200, overdue = 30 days",
        bps = listOf(BP(systolic = 120, diastolic = 80)),
        bloodSugars = listOf(
            BloodSugarReading("301", Random),
            BloodSugarReading("200", Random)
        ),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("5c075549-565b-483f-9682-c26cb4930570"),
        fullName = "BP = 200/80, RBS = 200, overdue = 30 days",
        bps = listOf(BP(systolic = 200, diastolic = 80)),
        bloodSugars = listOf(
            BloodSugarReading("250", Random),
            BloodSugarReading("200", Random)
        ),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("901f5be3-0b3e-4d5c-96a0-479f9124e4e5"),
        fullName = "BP = 120/80, RBS = 450, overdue = 30 days",
        bps = listOf(
            BP(systolic = 120, diastolic = 80),
            BP(systolic = 122, diastolic = 78)
        ),
        bloodSugars = listOf(BloodSugarReading("450", Random)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("629090a7-445b-4533-8bbf-55b99737d639"),
        fullName = "PPBS = 299, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("299", PostPrandial)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("8c42e506-0017-4029-aa89-0b968ca70c1a"),
        fullName = "PPBS = 300, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("300", PostPrandial)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("70a9af3b-a321-454f-b8b8-80cb48e87126"),
        fullName = "HbA1C = 8.9, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("8.9", HbA1c)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientBloodPressureAndBloodSugar(
        patientUuid = UUID.fromString("ac43a70f-4c62-4dec-abac-bbc1ec6fa515"),
        fullName = "HbA1C = 9, overdue = 30 days",
        bloodSugars = listOf(BloodSugarReading("9", HbA1c)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    // then
    val overdueAppointments = appointmentRepository.overdueAppointments(since = LocalDate.now(clock), facility = facility).blockingFirst()
    assertThat(overdueAppointments.map { it.fullName to it.isAtHighRisk }).isEqualTo(listOf(
        "Diastolic > 110, overdue == 3 days" to true,
        "FBS = 200, overdue = 30 days" to true,
        "FBS = 201, overdue = 30 days" to true,
        "RBS = 300, overdue = 30 days" to true,
        "RBS = 301, overdue = 30 days" to true,
        "BP = 200/80, RBS = 200, overdue = 30 days" to true,
        "BP = 120/80, RBS = 450, overdue = 30 days" to true,
        "PPBS = 300, overdue = 30 days" to true,
        "HbA1C = 9, overdue = 30 days" to true,
        "FBS = 199, overdue = 30 days" to false,
        "RBS = 299, overdue = 30 days" to false,
        "Latest RBS = 200, overdue = 30 days" to false,
        "BP = 120/80, RBS = 200, overdue = 30 days" to false,
        "PPBS = 299, overdue = 30 days" to false,
        "HbA1C = 8.9, overdue = 30 days" to false
    ))
  }

  @Test
  fun medical_diagnosis_of_patients_has_to_be_retrieved_properly() {
    data class MedicalHistoryAnswers(
        val hasDiabetes: Answer,
        val hasHypertension: Answer
    )

    fun savePatientDiagnosis(
        patientUuid: UUID,
        appointmentUuid: UUID,
        fullName: String,
        medicalHistoryAnswers: MedicalHistoryAnswers?,
        medicalHistoryUuid: () -> UUID,
        appointmentHasBeenOverdueFor: Duration
    ) {
      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          patientName = fullName,
          generatePhoneNumber = true,
          generateBusinessId = false
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val scheduledDate = (LocalDateTime.now(clock) - appointmentHasBeenOverdueFor).toLocalDate()
      appointmentRepository.schedule(
          patientUuid = patientUuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = scheduledDate,
          appointmentType = Manual,
          appointmentFacilityUuid = facility.uuid,
          creationFacilityUuid = facility.uuid
      )

      val bpTimestamp = Instant.now(clock)

      val bloodPressureMeasurement = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          systolic = 200,
          diastolic = 200,
          userUuid = user.uuid,
          facilityUuid = facility.uuid,
          recordedAt = bpTimestamp,
          createdAt = bpTimestamp,
          updatedAt = bpTimestamp
      )

      bpRepository.save(listOf(bloodPressureMeasurement)).blockingAwait()

      medicalHistoryAnswers?.run {
        medicalHistoryRepository.save(
            uuid = medicalHistoryUuid(),
            patientUuid = patientUuid,
            historyEntry = OngoingMedicalHistoryEntry(
                hasDiabetes = hasDiabetes,
                diagnosedWithHypertension = hasHypertension
            )
        ).blockingAwait()
      }
    }

    // when
    val thirtyDays = Duration.ofDays(30)

    savePatientDiagnosis(
        patientUuid = UUID.fromString("466016d6-f3fd-4982-b960-363f6c76a6b0"),
        appointmentUuid = UUID.fromString("d33e5ecf-54f6-49e9-b8c3-17d2d69b84cf"),
        fullName = "No diabetes; No hypertension",
        medicalHistoryAnswers = MedicalHistoryAnswers(
            hasDiabetes = No,
            hasHypertension = No
        ),
        medicalHistoryUuid = { UUID.fromString("fdf28ece-ddcd-4c51-8967-1d82250656d7") },
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientDiagnosis(
        patientUuid = UUID.fromString("599a9f6b-4bb4-4b67-9b3e-8975fe3a2199"),
        appointmentUuid = UUID.fromString("0e40b695-8fdd-4aa7-bf0d-2096ee0d9216"),
        fullName = "No diabetes; Has hypertension",
        medicalHistoryAnswers = MedicalHistoryAnswers(
            hasDiabetes = No,
            hasHypertension = Yes
        ),
        medicalHistoryUuid = { UUID.fromString("c9bcb81c-2f3f-4a59-adb8-137c3629124a") },
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientDiagnosis(
        patientUuid = UUID.fromString("6ee27ce7-9492-41cd-8cec-936087de615c"),
        appointmentUuid = UUID.fromString("0e175b99-837d-4147-adb8-3754a470abff"),
        fullName = "Has diabetes; No hypertension",
        medicalHistoryAnswers = MedicalHistoryAnswers(
            hasDiabetes = Yes,
            hasHypertension = No
        ),
        medicalHistoryUuid = { UUID.fromString("ad968719-d9c2-42be-9129-6fb766b595f6") },
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientDiagnosis(
        patientUuid = UUID.fromString("9d1c4d05-fbed-475a-8e31-c9014ce66c9c"),
        appointmentUuid = UUID.fromString("f0069f89-b9a6-4b0f-8493-64afa659eb47"),
        fullName = "Unanswered diabetes; Unanswered hypertension",
        medicalHistoryAnswers = MedicalHistoryAnswers(
            hasDiabetes = Unanswered,
            hasHypertension = Unanswered
        ),
        medicalHistoryUuid = { UUID.fromString("4220001b-8bcd-4d9a-97b3-0cff9c41fb84") },
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientDiagnosis(
        patientUuid = UUID.fromString("c3602eb2-d146-469b-b73a-f8336fd803c8"),
        appointmentUuid = UUID.fromString("51d88a67-291a-4f42-8606-3560cce215e5"),
        fullName = "No medical history",
        medicalHistoryAnswers = null,
        medicalHistoryUuid = { UUID.fromString("8de704b5-c2f7-4249-8026-a7670f5add9b") },
        appointmentHasBeenOverdueFor = thirtyDays
    )

    // then
    val overdueAppointments = appointmentRepository.overdueAppointments(since = LocalDate.now(clock), facility = facility).blockingFirst()

    data class MedicalHistoryResult(
        val name: String,
        val diagnosedWithDiabetes: Answer?,
        val diagnosedWithHypertension: Answer?
    )
    assertThat(overdueAppointments.map {
      MedicalHistoryResult(
          name = it.fullName,
          diagnosedWithDiabetes = it.diagnosedWithDiabetes,
          diagnosedWithHypertension = it.diagnosedWithHypertension
      )
    }).isEqualTo(listOf(
        MedicalHistoryResult(name = "No diabetes; No hypertension", diagnosedWithDiabetes = No, diagnosedWithHypertension = No),
        MedicalHistoryResult(name = "No diabetes; Has hypertension", diagnosedWithDiabetes = No, diagnosedWithHypertension = Yes),
        MedicalHistoryResult(name = "Has diabetes; No hypertension", diagnosedWithDiabetes = Yes, diagnosedWithHypertension = No),
        MedicalHistoryResult(name = "Unanswered diabetes; Unanswered hypertension", diagnosedWithDiabetes = Unanswered,
            diagnosedWithHypertension = Unanswered),
        MedicalHistoryResult(name = "No medical history", diagnosedWithDiabetes = null, diagnosedWithHypertension = null)
    ))
  }

  @Suppress("LocalVariableName")
  @Test
  fun fetching_the_latest_overdue_appointment_for_a_patient_should_get_the_latest_scheduled_appointment_which_is_past_the_scheduled_date() {
    // given
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        generatePhoneNumber = true
    )
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val today = LocalDate.now(clock)
    val aWeekInThePast = today.minusWeeks(1)
    val aWeekInFuture = today.plusWeeks(1)
    val twoWeeksInFuture = today.plusWeeks(2)

    val bp_recorded_a_week_ago = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("b916ca18-3e60-4e3c-a1b9-46504ddf0662"),
        recordedAt = aWeekInThePast.toUtcInstant(userClock),
        patientUuid = patientUuid,
        userUuid = user.uuid,
        facilityUuid = facility.uuid
    )

    val appointment_scheduled_for_today = TestData.appointment(
        uuid = UUID.fromString("d7c7fdca-74e2-4248-93ea-ffb57c81c995"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        status = Scheduled,
        scheduledDate = today
    )
    val appointment_scheduled_a_week_in_the_future = TestData.appointment(
        uuid = UUID.fromString("81ba3bfc-2579-43fe-9af8-7de79a75d37d"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        status = Scheduled,
        scheduledDate = aWeekInFuture
    )
    val visited_appointment_two_weeks_in_the_future = TestData.appointment(
        uuid = UUID.fromString("96cc19f4-44c3-45f4-a60a-50c55ea78445"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        status = Visited,
        scheduledDate = twoWeeksInFuture
    )

    bpRepository.save(listOf(
        bp_recorded_a_week_ago
    )).blockingAwait()

    appointmentRepository.save(listOf(
        appointment_scheduled_for_today,
        appointment_scheduled_a_week_in_the_future,
        visited_appointment_two_weeks_in_the_future
    )).blockingAwait()

    // then
    val latest_appointment_today = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, today.plusDays(1)) as Just
    assertThat(latest_appointment_today.value.appointment).isEqualTo(appointment_scheduled_for_today)

    val latest_appointment_a_week_later = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, aWeekInFuture.plusDays(1)) as Just
    assertThat(latest_appointment_a_week_later.value.appointment).isEqualTo(appointment_scheduled_a_week_in_the_future)

    val latest_appointment_two_weeks_later = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, twoWeeksInFuture.plusDays(1)) as Just
    assertThat(latest_appointment_two_weeks_later.value.appointment).isEqualTo(appointment_scheduled_a_week_in_the_future)
  }

  @Suppress("LocalVariableName")
  @Test
  fun fetching_the_latest_overdue_appointment_for_a_patient_should_account_for_the_reminder_date() {
    // given
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        generatePhoneNumber = true
    )
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val today = LocalDate.now(clock)
    val aWeekInThePast = today.minusWeeks(1)
    val aWeekInFuture = today.plusWeeks(1)

    val bp_recorded_a_week_ago = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("b916ca18-3e60-4e3c-a1b9-46504ddf0662"),
        recordedAt = aWeekInThePast.toUtcInstant(userClock),
        patientUuid = patientUuid,
        userUuid = user.uuid,
        facilityUuid = facility.uuid
    )

    val appointment_scheduled_for_today_with_reminder_a_week_in_the_future = TestData.appointment(
        uuid = UUID.fromString("d7c7fdca-74e2-4248-93ea-ffb57c81c995"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        status = Scheduled,
        scheduledDate = today,
        remindOn = aWeekInFuture
    )

    bpRepository.save(listOf(
        bp_recorded_a_week_ago
    )).blockingAwait()

    appointmentRepository.save(listOf(
        appointment_scheduled_for_today_with_reminder_a_week_in_the_future
    )).blockingAwait()

    // then
    val latest_appointment_today = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, today.plusDays(1))
    assertThat(latest_appointment_today).isEqualTo(None<OverdueAppointment>())

    val latest_appointment_a_week_later = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, aWeekInFuture.plusDays(1)) as Just
    assertThat(latest_appointment_a_week_later.value.appointment).isEqualTo(appointment_scheduled_for_today_with_reminder_a_week_in_the_future)
  }

  @Test
  fun fetching_overdue_appointments_count_should_work_correctly() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        scheduledDate: LocalDate,
        facilityUuid: UUID
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp)).blockingAwait()

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bloodSugarRepository.save(listOf(bloodSugar)).blockingAwait()

      val appointment = TestData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment)).blockingAwait()
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("9b794e72-6ebb-48c3-a8d7-69751ffeecc2")
    val patientWithTenDaysOverdue = UUID.fromString("0fc57e45-7018-4c03-9218-f90f6fc0f268")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("51467803-f588-4a65-8def-7a15f41bdd13")

    val now = LocalDate.now(clock)
    val facilityUuid = UUID.fromString("ccc66ec1-5029-455b-bf92-caa6d90a9a79")

    val facility = TestData.facility(uuid = facilityUuid)

    createOverdueAppointment(patientWithOneDayOverdue, now.minusDays(1), facilityUuid)
    createOverdueAppointment(patientWithTenDaysOverdue, now.minusDays(10), facilityUuid)
    createOverdueAppointment(patientWithOverAnYearDaysOverdue, now.minusDays(370), facilityUuid)

    //when
    val overdueAppointmentsCount = appointmentRepository.overdueAppointmentsCount(since = now, facility = facility).blockingFirst()

    //then
    assertThat(overdueAppointmentsCount).isEqualTo(2)
  }

  @Test
  fun fetching_overdue_appointments_should_work_correctly() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        scheduledDate: LocalDate,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp)).blockingAwait()

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bloodSugarRepository.save(listOf(bloodSugar)).blockingAwait()

      val appointment = TestData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment)).blockingAwait()
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("1e5f206b-2bc0-4e5e-bbbe-4f4a1bdaca53")
    val patientWithFiveDayOverdue = UUID.fromString("1105bc5a-3e2d-4efd-bcd3-1fb22dd1461f")
    val patientWithTenDaysOverdue = UUID.fromString("50604030-303a-4979-bfda-297c169ed929")
    val patientWithFifteenDaysOverdue = UUID.fromString("a4e5c0e0-cacd-49bf-8663-ab4d19435c3b")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("9e1c9dae-6bea-4463-8ad8-609d9118e20d")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("4f3c6c64-3a2b-4f18-9179-978c2aa0b698")
    val facility2Uuid = UUID.fromString("ca52615f-402d-48f1-a063-4d6af19baaa6")

    val assignedFacility1Uuid = facility1Uuid
    val assignedFacility2Uuid = facility2Uuid

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2)).blockingAwait()

    createOverdueAppointment(
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = assignedFacility1Uuid
    )
    createOverdueAppointment(
        patientUuid = patientWithFiveDayOverdue,
        scheduledDate = now.minusDays(5),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null
    )
    createOverdueAppointment(
        patientUuid = patientWithTenDaysOverdue,
        scheduledDate = now.minusDays(10),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = assignedFacility2Uuid
    )
    createOverdueAppointment(
        patientUuid = patientWithFifteenDaysOverdue,
        scheduledDate = now.minusDays(15),
        facilityUuid = facility2Uuid,
        patientAssignedFacilityUuid = assignedFacility1Uuid
    )
    createOverdueAppointment(
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null
    )

    //when
    val overdueAppointments = appointmentRepository.overdueAppointments(since = now, facility = facility1).blockingFirst().map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(patientWithOneDayOverdue, patientWithFiveDayOverdue, patientWithFifteenDaysOverdue))
  }

  @Test
  fun fetching_overdue_appointments_with_appointment_facility_names_should_work_correctly() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        scheduledDate: LocalDate,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid
      )
      patientRepository.save(listOf(patientProfile)).blockingAwait()

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp)).blockingAwait()

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bloodSugarRepository.save(listOf(bloodSugar)).blockingAwait()

      val appointment = TestData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment)).blockingAwait()
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("6c314875-dc2f-42a0-86f0-e883e5f17043")
    val patientWithTenDaysOverdue = UUID.fromString("f03f2c7c-14b3-429d-b69a-6d072a42173d")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("f90ef167-b673-4ad6-ae3c-f1dafd82e1e9")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("a347eee6-d1ea-4ab8-84b9-a5166f0c11a4")
    val facility2Uuid = UUID.fromString("ce1fa1ae-02af-49a9-91af-f659a6573e5a")

    val assignedFacilityUuid = facility2Uuid

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2)).blockingAwait()

    createOverdueAppointment(
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = assignedFacilityUuid
    )
    createOverdueAppointment(
        patientUuid = patientWithTenDaysOverdue,
        scheduledDate = now.minusDays(10),
        facilityUuid = facility2Uuid,
        patientAssignedFacilityUuid = null
    )
    createOverdueAppointment(
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null
    )

    //when
    val overdueAppointments = appointmentRepository.overdueAppointments(since = now, facility = facility2).blockingFirst().map { it.appointmentFacilityName }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf("PHC Obvious", "PHC Bagta"))
  }

  private fun markAppointmentSyncStatusAsDone(vararg appointmentUuids: UUID) {
    appointmentRepository.setSyncStatus(appointmentUuids.toList(), DONE)
  }

  private fun getAppointmentByUuid(appointmentUuid: UUID): Appointment {
    return database.appointmentDao().getOne(appointmentUuid)!!
  }

  data class RecordAppointment(
      val patientProfile: PatientProfile,
      val bloodPressureMeasurement: BloodPressureMeasurement?,
      val bloodSugarMeasurement: BloodSugarMeasurement?,
      val appointment: Appointment
  ) {
    fun save(
        patientRepository: PatientRepository,
        bloodPressureRepository: BloodPressureRepository,
        bloodSugarRepository: BloodSugarRepository,
        appointmentRepository: AppointmentRepository
    ) {
      val saveBp = if (bloodPressureMeasurement != null) {
        bloodPressureRepository.save(listOf(bloodPressureMeasurement))
      } else Completable.complete()

      val saveBloodSugar = if (bloodSugarMeasurement != null) {
        bloodSugarRepository.save(listOf(bloodSugarMeasurement))
      } else Completable.complete()

      patientRepository.save(listOf(patientProfile))
          .andThen(saveBp)
          .andThen(saveBloodSugar)
          .andThen(appointmentRepository.save(listOf(appointment)))
          .blockingAwait()
    }

    fun toOverdueAppointment(appointmentFacilityName: String?): OverdueAppointment {
      if (bloodPressureMeasurement == null && bloodSugarMeasurement == null) {
        throw AssertionError("Need a Blood Pressure Measurement or Blood Sugar Measurement to create an Overdue Appointment")
      } else {
        val patientLastSeen = when {
          bloodPressureMeasurement == null -> bloodSugarMeasurement!!.recordedAt
          bloodSugarMeasurement == null -> bloodPressureMeasurement.recordedAt
          else -> maxOf(bloodPressureMeasurement.recordedAt, bloodSugarMeasurement.recordedAt)
        }
        val overduePatientAddress = OverduePatientAddress(
            streetAddress = patientProfile.address.streetAddress,
            colonyOrVillage = patientProfile.address.colonyOrVillage,
            district = patientProfile.address.district,
            state = patientProfile.address.state
        )
        return OverdueAppointment(
            fullName = patientProfile.patient.fullName,
            gender = patientProfile.patient.gender,
            dateOfBirth = patientProfile.patient.dateOfBirth,
            age = patientProfile.patient.age,
            appointment = appointment,
            phoneNumber = patientProfile.phoneNumbers.first(),
            patientAddress = overduePatientAddress,
            isAtHighRisk = false,
            patientLastSeen = patientLastSeen,
            diagnosedWithDiabetes = null,
            diagnosedWithHypertension = null,
            patientAssignedFacilityUuid = patientProfile.patient.assignedFacilityId,
            appointmentFacilityName = appointmentFacilityName
        )
      }
    }
  }
}

private fun PatientPhoneNumber.withPatientUuid(uuid: UUID): PatientPhoneNumber {
  return this.copy(patientUuid = uuid)
}
