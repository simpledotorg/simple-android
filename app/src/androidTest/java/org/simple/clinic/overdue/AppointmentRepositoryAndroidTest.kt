package org.simple.clinic.overdue

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.PagingTestCase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.assertValues
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.home.overdue.OverdueAppointment_Old
import org.simple.clinic.home.overdue.OverduePatientAddress
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.Appointment.Status.Visited
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.overdue.callresult.Outcome.AgreedToVisit
import org.simple.clinic.overdue.callresult.Outcome.RemindToCallLater
import org.simple.clinic.overdue.callresult.Outcome.RemovedFromOverdueList
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.PatientStatus.Active
import org.simple.clinic.patient.PatientStatus.Dead
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.summary.nextappointment.NextAppointmentPatientProfile
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.toNullable
import org.simple.clinic.util.toUtcInstant
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.sharedTestCode.util.TestUtcClock
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
  lateinit var callResultRepository: CallResultRepository

  @Inject
  lateinit var prescriptionRepository: PrescriptionRepository

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
      .around(SaveDatabaseRule())

  private val patientUuid = UUID.fromString("fcf0acd3-0b09-4ecb-bcd4-af40ca6456fc")
  private val appointmentUuid = UUID.fromString("a374e38f-6bc3-4829-899c-0966a4e13b10")

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.parse("2018-01-01"))
    userClock.setDate(LocalDate.parse("2018-01-01"))
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

    patientRepository.save(patients)

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

    // when
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = today,
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data
        .associateBy { it.appointment.patientUuid }

    // then
    assertThat(overdueAppointments.keys).containsExactly(noBpsDeletedPatientUuid, latestBpDeletedPatientUuid, oldestBpNotDeletedPatientUuid)
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

    patientRepository.save(patients)

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

    // when
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = today,
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data
        .associateBy { it.appointment.patientUuid }

    // then
    assertThat(overdueAppointments.keys).containsExactly(noBloodSugarsDeletedPatientUuid, latestBloodSugarDeletedPatientUuid, oldestBloodSugarNotDeletedPatientUuid)
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
          generatePhoneNumber = true,
          generateBusinessId = false,
          patientName = fullName
      )
      patientRepository.save(listOf(patientProfile))

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
      bpRepository.save(bloodPressureMeasurements)

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
    val appointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = LocalDate.now(clock),
        facilityId = facility.uuid),
        loadSize = 15)
        .loadPage()
        .data

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
      patientRepository.save(listOf(patientProfile))

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp))

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment))
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
    val overduePatientUuids = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = now,
        facilityId = facilityUuid),
        loadSize = 10)
        .loadPage()
        .data
        .map { it.appointment.patientUuid }

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
    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)
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
    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)

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
      patientRepository.save(listOf(patientProfile))

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp))

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = LocalDate.now(clock).minusDays(1),
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment))
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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = LocalDate.now(clock),
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data

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
      patientRepository.save(listOf(patientProfile))

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp))

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = LocalDate.now(clock).minusDays(1),
          status = Scheduled,
          cancelReason = null,
          deletedAt = if (isAppointmentDeleted) Instant.now() else null
      )
      appointmentRepository.save(listOf(appointment))
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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = LocalDate.now(clock),
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data

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
          generatePhoneNumber = true,
          patientRegisteredFacilityId = facility.uuid
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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = currentDate,
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data

    // then
    val expectedAppointments = listOf(oneWeekBeforeCurrentDate, oneDayBeforeCurrentDate).map { it.toOverdueAppointment(appointmentFacilityName = facility.name, registeredFacilityName = facility.name) }

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
          generatePhoneNumber = true,
          patientRegisteredFacilityId = facility.uuid
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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = currentDate,
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data

    // then
    val expectedAppointments = listOf(remindOneWeekBeforeCurrentDate, remindOneDayBeforeCurrentDate).map { it.toOverdueAppointment(appointmentFacilityName = facility.name, registeredFacilityName = facility.name) }

    assertThat(overdueAppointments).containsExactlyElementsIn(expectedAppointments)
  }

  @Test
  fun patients_without_phone_number_should_be_shown_when_fetching_overdue_appointments() {

    val currentDate = LocalDate.parse("2018-01-05")

    fun createAppointmentRecord(
        patientUuid: UUID,
        bpUuid: UUID,
        appointmentUuid: UUID,
        patientPhoneNumber: PatientPhoneNumber?
    ): RecordAppointment {
      val patientProfile = with(testData.patientProfile(patientUuid = patientUuid, generatePhoneNumber = false, patientRegisteredFacilityId = facility.uuid)) {
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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = currentDate,
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data

    // then
    val expectedAppointments = listOf(withPhoneNumber, withoutPhoneNumber).map { it.toOverdueAppointment(appointmentFacilityName = facility.name, registeredFacilityName = facility.name) }

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
      val patientProfile = testData.patientProfile(patientUuid = patientUuid, generatePhoneNumber = true, patientRegisteredFacilityId = facility.uuid)

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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = currentDate,
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data

    // then
    val expectedAppointments = listOf(withBloodPressure).map { it.toOverdueAppointment(appointmentFacilityName = facility.name, registeredFacilityName = facility.name) }

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
      val patientProfile = testData.patientProfile(patientUuid = patientUuid, generatePhoneNumber = true, patientRegisteredFacilityId = facility.uuid)

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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = currentDate,
        facilityId = facility.uuid),
        loadSize = 10)
        .loadPage()
        .data

    // then
    val expectedAppointments = listOf(withBloodSugar).map { it.toOverdueAppointment(appointmentFacilityName = facility.name, registeredFacilityName = facility.name) }

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
          generatePhoneNumber = true,
          generateBusinessId = false,
          patientName = fullName
      )
      patientRepository.save(listOf(patientProfile))

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
      bpRepository.save(bloodPressureMeasurements)

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
      bloodSugarRepository.save(bloodSugarMeasurements)

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
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = LocalDate.now(clock),
        facilityId = facility.uuid),
        loadSize = 15)
        .loadPage()
        .data
        .map { it.fullName to it.isAtHighRisk }

    assertThat(overdueAppointments).isEqualTo(listOf(
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

  @Suppress("LocalVariableName")
  @Test
  fun fetching_the_latest_overdue_appointment_for_a_patient_should_get_the_latest_scheduled_appointment_which_is_past_the_scheduled_date() {
    // given
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        generatePhoneNumber = true
    )
    patientRepository.save(listOf(patientProfile))

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
        scheduledDate = today,
        createdAt = today.toUtcInstant(userClock),
        updatedAt = today.toUtcInstant(userClock),
        deletedAt = null
    )
    val appointment_scheduled_a_week_in_the_future = TestData.appointment(
        uuid = UUID.fromString("81ba3bfc-2579-43fe-9af8-7de79a75d37d"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        status = Scheduled,
        scheduledDate = aWeekInFuture,
        createdAt = aWeekInFuture.toUtcInstant(userClock),
        updatedAt = aWeekInFuture.toUtcInstant(userClock),
        deletedAt = null
    )
    val visited_appointment_two_weeks_in_the_future = TestData.appointment(
        uuid = UUID.fromString("96cc19f4-44c3-45f4-a60a-50c55ea78445"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        status = Visited,
        scheduledDate = twoWeeksInFuture,
        createdAt = twoWeeksInFuture.toUtcInstant(userClock),
        updatedAt = twoWeeksInFuture.toUtcInstant(userClock),
        deletedAt = null
    )

    bpRepository.save(listOf(
        bp_recorded_a_week_ago
    ))

    appointmentRepository.save(listOf(
        appointment_scheduled_for_today,
        appointment_scheduled_a_week_in_the_future,
        visited_appointment_two_weeks_in_the_future
    ))

    // then
    val latest_appointment_today = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, today.plusDays(1))
    assertThat(latest_appointment_today.get()).isEqualTo(appointment_scheduled_for_today)

    val latest_appointment_a_week_later = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, aWeekInFuture.plusDays(1))
    assertThat(latest_appointment_a_week_later.get()).isEqualTo(appointment_scheduled_a_week_in_the_future)

    val latest_appointment_two_weeks_later = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, twoWeeksInFuture.plusDays(1))
    assertThat(latest_appointment_two_weeks_later.get()).isEqualTo(appointment_scheduled_a_week_in_the_future)
  }

  @Test
  fun patient_that_are_marked_as_dead_should_not_be_present_when_loading_overdue_appointments() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        scheduledDate: LocalDate,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentId: UUID
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientStatus = patientStatus,
          patientAssignedFacilityId = patientAssignedFacilityUuid
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentId,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment))
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("6c314875-dc2f-42a0-86f0-e883e5f17043")
    val patientWithTenDaysOverdue = UUID.fromString("f03f2c7c-14b3-429d-b69a-6d072a42173d")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("f90ef167-b673-4ad6-ae3c-f1dafd82e1e9")
    val deadPatient = UUID.fromString("34441291-a3ad-483e-bf8e-5f1097c7749b")

    val appointmentWithOneDayOverdue = UUID.fromString("68984008-e10f-43bd-93e8-b4720c444a4c")
    val appointmentWithTenDaysOverdue = UUID.fromString("b4fb2e7a-4917-4813-8271-8c5160729370")
    val appointmentWithOverAnYearDaysOverdue = UUID.fromString("41b46be7-4d39-4c49-a588-05947d39a78a")
    val appointmentForDeadPatient = UUID.fromString("ea332816-a500-424a-8fb2-5f2fa538da49")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("a347eee6-d1ea-4ab8-84b9-a5166f0c11a4")
    val facility2Uuid = UUID.fromString("ce1fa1ae-02af-49a9-91af-f659a6573e5a")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility2Uuid,
        patientStatus = Active,
        appointmentId = appointmentWithOneDayOverdue
    )
    createOverdueAppointment(
        patientUuid = patientWithTenDaysOverdue,
        scheduledDate = now.minusDays(10),
        facilityUuid = facility2Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentId = appointmentWithTenDaysOverdue
    )
    createOverdueAppointment(
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentId = appointmentWithOverAnYearDaysOverdue
    )
    createOverdueAppointment(
        patientUuid = deadPatient,
        scheduledDate = now.minusDays(20),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Dead,
        appointmentId = appointmentForDeadPatient
    )

    //when
    val overdueAppointments = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = now,
        facilityId = facility2.uuid),
        loadSize = 10)
        .loadPage()
        .data
        .map { it.appointment.uuid }

    //then
    assertThat(overdueAppointments).containsExactly(
        appointmentWithOneDayOverdue,
        appointmentWithTenDaysOverdue
    )
  }

  @Test
  fun fetching_overdue_appointments_in_a_facility_should_work_correctly() {
    fun createOverdueAppointment(
        patientUuid: UUID,
        scheduledDate: LocalDate,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        status: Status,
        createdAt: Instant,
        updatedAt: Instant,
        deletedAt: Instant?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = status,
          cancelReason = null,
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      )
      appointmentRepository.save(listOf(appointment))
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("1e5f206b-2bc0-4e5e-bbbe-4f4a1bdaca53")
    val patientWithFiveDayOverdue = UUID.fromString("1105bc5a-3e2d-4efd-bcd3-1fb22dd1461f")
    val patientWithTenDaysOverdue = UUID.fromString("50604030-303a-4979-bfda-297c169ed929")
    val patientWithFifteenDaysOverdue = UUID.fromString("a4e5c0e0-cacd-49bf-8663-ab4d19435c3b")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("9e1c9dae-6bea-4463-8ad8-609d9118e20d")
    val patientWithMultipleOverdueAppointments = UUID.fromString("c3159d7b-3327-4203-935e-2acbe3068d69")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("4f3c6c64-3a2b-4f18-9179-978c2aa0b698")
    val facility2Uuid = UUID.fromString("ca52615f-402d-48f1-a063-4d6af19baaa6")

    val assignedFacility1Uuid = facility1Uuid
    val assignedFacility2Uuid = facility2Uuid

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = assignedFacility1Uuid,
        status = Scheduled,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null
    )
    createOverdueAppointment(
        patientUuid = patientWithFiveDayOverdue,
        scheduledDate = now.minusDays(5),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        status = Scheduled,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null
    )
    createOverdueAppointment(
        patientUuid = patientWithTenDaysOverdue,
        scheduledDate = now.minusDays(10),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = assignedFacility2Uuid,
        status = Scheduled,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null
    )
    createOverdueAppointment(
        patientUuid = patientWithFifteenDaysOverdue,
        scheduledDate = now.minusDays(15),
        facilityUuid = facility2Uuid,
        patientAssignedFacilityUuid = assignedFacility1Uuid,
        status = Scheduled,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null
    )
    createOverdueAppointment(
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        status = Scheduled,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null
    )
    createOverdueAppointment(
        patientUuid = patientWithMultipleOverdueAppointments,
        scheduledDate = now.minusDays(3),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = assignedFacility1Uuid,
        status = Scheduled,
        createdAt = Instant.parse("2018-02-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-02-01T00:00:00Z"),
        deletedAt = null
    )

    //when
    val overduePatients = PagingTestCase(pagingSource = appointmentRepository.overdueAppointmentsInFacility(since = now,
        facilityId = facility1.uuid),
        loadSize = 10)
        .loadPage()
        .data
        .map { it.appointment.patientUuid }

    //then
    assertThat(overduePatients).isEqualTo(listOf(
        patientWithOneDayOverdue,
        patientWithMultipleOverdueAppointments,
        patientWithFiveDayOverdue,
        patientWithFifteenDaysOverdue
    ))
  }

  @Test
  fun fetching_next_appointment_patient_profile_should_work_correctly() {
    // given
    val now = LocalDate.now(clock)
    val patientUuid = UUID.fromString("4de4dd8f-eefa-4693-8eaf-56beb1da4f1d")
    val visitedAppointment = TestData.appointment(
        uuid = UUID.fromString("2da3673e-94be-4b1c-b667-ca03a14d6ebf"),
        patientUuid = patientUuid,
        scheduledDate = now.minusDays(30),
        status = Visited,
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock),
        deletedAt = null,
        cancelReason = null,
        agreedToVisit = true,
        facilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid,
        appointmentType = Manual
    )

    val scheduledAppointment = TestData.appointment(
        uuid = UUID.fromString("230b2e75-2d4a-4bf9-bd4e-83899c83ec54"),
        patientUuid = patientUuid,
        scheduledDate = now.plusDays(10),
        status = Scheduled,
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock),
        deletedAt = null,
        cancelReason = null,
        agreedToVisit = null,
        facilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid,
        appointmentType = Manual
    )

    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)
    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(scheduledAppointment, patientProfile.patient, facility)

    patientRepository.save(listOf(patientProfile))
    facilityRepository.save(listOf(facility))
    appointmentRepository.save(listOf(visitedAppointment, scheduledAppointment))

    // when
    val expectedAppointment = appointmentRepository.nextAppointmentPatientProfile(patientUuid)

    // then
    assertThat(expectedAppointment).isEqualTo(nextAppointmentPatientProfile)
  }

  @Test
  fun should_not_fetch_automatic_appointment_when_fetching_next_appointment_patient_profile() {
    // given
    val now = LocalDate.now(clock)
    val patientUuid = UUID.fromString("5ac54fc7-437e-40e9-bc58-8cf61c930472")
    val visitedAppointment = TestData.appointment(
        uuid = UUID.fromString("cfe7df95-b17a-4254-88c1-fb672bfc8023"),
        patientUuid = patientUuid,
        scheduledDate = now.minusDays(30),
        status = Visited,
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock),
        deletedAt = null,
        cancelReason = null,
        agreedToVisit = true,
        facilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid,
        appointmentType = Manual
    )

    val scheduledAutomaticAppointment = TestData.appointment(
        uuid = UUID.fromString("6c3acd2b-bf01-4712-8b44-f2000b2eb227"),
        patientUuid = patientUuid,
        scheduledDate = now.plusDays(10),
        status = Scheduled,
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock),
        deletedAt = null,
        cancelReason = null,
        agreedToVisit = null,
        facilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid,
        appointmentType = Automatic
    )

    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)

    patientRepository.save(listOf(patientProfile))
    facilityRepository.save(listOf(facility))
    appointmentRepository.save(listOf(visitedAppointment, scheduledAutomaticAppointment))

    // when
    val expectedAppointment = appointmentRepository.nextAppointmentPatientProfile(patientUuid)

    // then
    assertThat(expectedAppointment).isNull()
  }

  @Test
  fun querying_whether_appointment_for_patient_has_changed_should_work_as_expected() {
    fun setAppointmentSyncStatusToDone(appointmentId: UUID) {
      database.appointmentDao().updateSyncStatusForIds(listOf(appointmentId), DONE)
    }

    val patientUuid = UUID.fromString("efd303fd-f96b-4b05-9c8a-c067b189974e")
    val now = Instant.now(clock)
    val oneSecondEarlier = now.minus(Duration.ofSeconds(1))
    val fiftyNineSecondsLater = now.plus(Duration.ofSeconds(59))
    val oneMinuteLater = now.plus(Duration.ofMinutes(1))
    val fiveMinuteLater = now.plus(Duration.ofMinutes(5))

    val appointment1ForPatient = TestData.appointment(
        uuid = UUID.fromString("84703ef1-7e50-44d0-83a0-ea931dacccf7"),
        patientUuid = patientUuid,
        syncStatus = PENDING,
        updatedAt = now,
        status = Scheduled
    )
    val appointment2ForPatient = TestData.appointment(
        uuid = UUID.fromString("6b0a7ded-9fe8-4ac6-9103-94674d3b72f9"),
        patientUuid = patientUuid,
        syncStatus = PENDING,
        updatedAt = oneMinuteLater,
        status = Scheduled
    )
    val appointmentForSomeOtherPatient = TestData.appointment(
        uuid = UUID.fromString("a39b9a71-40cd-42bc-bfeb-0c6055426e24"),
        patientUuid = UUID.fromString("c77a152e-223c-469a-8bff-a568ddfde628"),
        syncStatus = PENDING,
        updatedAt = now,
        status = Scheduled
    )
    val visitedAppointmentForPatient = TestData.appointment(
        uuid = UUID.fromString("94fc4d8b-d6ba-4c90-89fd-5538dd6f565b"),
        patientUuid = UUID.fromString("a73dafab-0317-4dbf-8dc5-3e48e15325fd"),
        syncStatus = PENDING,
        updatedAt = fiveMinuteLater,
        status = Visited
    )

    database.appointmentDao().save(listOf(appointment1ForPatient, appointment2ForPatient, appointmentForSomeOtherPatient, visitedAppointmentForPatient))

    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, now)).isTrue()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, fiftyNineSecondsLater)).isTrue()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, oneMinuteLater)).isFalse()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, fiveMinuteLater)).isFalse()

    setAppointmentSyncStatusToDone(appointment2ForPatient.uuid)
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, fiftyNineSecondsLater)).isFalse()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()

    setAppointmentSyncStatusToDone(appointment1ForPatient.uuid)
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, oneSecondEarlier)).isFalse()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(appointmentForSomeOtherPatient.patientUuid, oneSecondEarlier)).isTrue()

    setAppointmentSyncStatusToDone(appointmentForSomeOtherPatient.uuid)
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(appointmentForSomeOtherPatient.patientUuid, oneSecondEarlier)).isFalse()
  }

  @Test
  fun marking_older_appointments_as_visited_for_a_patient_should_work_as_expected() {

    fun scheduledAppointmentsForPatient(patientUuid: PatientUuid): List<Appointment> {
      return appointmentRepository.getAllAppointmentsForPatient(patientUuid)
          .filter { it.status == Scheduled }
    }

    // given
    val patient1 = UUID.fromString("1c36c4d3-c968-4ffc-90cd-4a1223b23634")
    val patient2 = UUID.fromString("442c8244-d0e3-4e8c-a6ab-0336dfc76e97")

    val threeMonthsOldAppointmentForPatient1 = TestData.appointment(
        uuid = UUID.fromString("10d55987-f443-4094-b5e1-57327adea42b"),
        status = Scheduled,
        syncStatus = DONE,
        patientUuid = patient1,
        cancelReason = null
    )

    val twoMonthsOldAppointmentForPatient1 = TestData.appointment(
        uuid = UUID.fromString("b00f842e-d402-4f61-8b82-b1fb437c9a23"),
        status = Visited,
        syncStatus = DONE,
        patientUuid = patient1,
        cancelReason = null
    )

    val oneMonthOldAppointmentForPatient1 = TestData.appointment(
        uuid = UUID.fromString("1c2cbb8f-eac6-4f5e-975e-eda42908cbd3"),
        status = Scheduled,
        syncStatus = DONE,
        patientUuid = patient1,
        cancelReason = null
    )

    val oneMonthOldAppointmentForPatient2 = TestData.appointment(
        uuid = UUID.fromString("569cc97a-949f-48d6-850e-e0baa5f66315"),
        status = Scheduled,
        syncStatus = DONE,
        patientUuid = patient2,
        cancelReason = null
    )

    appointmentRepository.save(listOf(
        threeMonthsOldAppointmentForPatient1,
        twoMonthsOldAppointmentForPatient1,
        oneMonthOldAppointmentForPatient1,
        oneMonthOldAppointmentForPatient2
    ))

    assertThat(scheduledAppointmentsForPatient(patient1)).isNotEmpty()

    // when
    appointmentRepository.markOlderAppointmentsAsVisited(patient1)

    // then
    assertThat(scheduledAppointmentsForPatient(patient1)).isEmpty()
  }

  @Test
  fun getting_latest_scheduled_appointment_for_a_patient_should_work_correctly() {
    // given
    val patientUuid = UUID.fromString("df4ccd88-aab6-42ff-ac52-40fb2291c000")
    val visitedAppointment = TestData.appointment(
        uuid = UUID.fromString("e02462dc-402a-4d81-bab9-c7a82f39d79f"),
        patientUuid = patientUuid,
        scheduledDate = LocalDate.parse("2018-02-01"),
        status = Visited,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null,
        cancelReason = null,
        agreedToVisit = true,
        facilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid,
        appointmentType = Manual
    )

    val visitedAppointment2 = TestData.appointment(
        uuid = UUID.fromString("230b2e75-2d4a-4bf9-bd4e-83899c83ec54"),
        patientUuid = patientUuid,
        scheduledDate = LocalDate.parse("2018-03-02"),
        status = Visited,
        createdAt = Instant.parse("2018-02-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-02-01T00:00:00Z"),
        deletedAt = null,
        cancelReason = null,
        agreedToVisit = null,
        facilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid,
        appointmentType = Manual
    )

    val scheduledAppointment = TestData.appointment(
        uuid = UUID.fromString("230b2e75-2d4a-4bf9-bd4e-83899c83ec54"),
        patientUuid = patientUuid,
        scheduledDate = LocalDate.parse("2018-03-01"),
        status = Scheduled,
        createdAt = Instant.parse("2018-02-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-02-01T00:00:00Z"),
        deletedAt = null,
        cancelReason = null,
        agreedToVisit = null,
        facilityUuid = facility.uuid,
        creationFacilityUuid = facility.uuid,
        appointmentType = Manual
    )

    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)

    patientRepository.save(listOf(patientProfile))
    facilityRepository.save(listOf(facility))
    appointmentRepository.save(listOf(visitedAppointment, visitedAppointment2, scheduledAppointment))

    // when
    val expectedAppointment = appointmentRepository.latestScheduledAppointmentForPatient(patientUuid)

    // then
    assertThat(expectedAppointment).isEqualTo(scheduledAppointment)
  }

  @Test
  fun getting_overdue_appointments_in_a_facility_should_work_correctly() {
    fun createOverdueAppointment(
        appointmentUuid: UUID,
        patientUuid: UUID,
        scheduledDate: LocalDate,
        remindOn: LocalDate?,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentStatus: Status,
        callResult: CallResult?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid,
          patientStatus = patientStatus
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = appointmentStatus,
          cancelReason = null,
          remindOn = remindOn
      )
      appointmentRepository.save(listOf(appointment))

      if (callResult != null) {
        callResultRepository.save(listOf(TestData.callResult(
            appointmentId = appointmentUuid,
            outcome = callResult.outcome,
            removeReason = callResult.removeReason
        )))
      }
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("8839c1cd-c07e-4064-8819-da8608f80287")
    val patientWithFiveDayOverdue = UUID.fromString("b992b37a-9b5d-4f40-b296-34416861b0df")
    val patientWithTenDaysOverdue = UUID.fromString("0f802d2b-953f-47d4-bc38-5b11c29f1638")
    val patientWithFifteenDaysOverdue = UUID.fromString("316fd1b9-9883-4528-8ce7-08f73a31d05c")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("bb8726ba-6bae-4c0d-a542-0f16743049c7")
    val deadPatientWithOverdue = UUID.fromString("1702e079-9eb5-4055-b21e-ed2adbd8bcf3")
    val patientWithVisitedAppointment = UUID.fromString("7dadac68-d2d0-4d83-9531-361277db4a34")
    val patientWithCancelledAppointment = UUID.fromString("51e03608-2274-4d8d-a4ba-da717095152a")
    val patientWithAppointmentRemindedInFiveDaysSinceOverdue = UUID.fromString("1c0d4154-433d-4168-ab84-309505805ebd")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("08f0d831-3d27-4e99-9404-b9d982ab3541")
    val facility2Uuid = UUID.fromString("0cdab726-8b44-4775-9158-8637329f2ad3")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("13f868ba-feaa-4cb6-8ea7-c4f4fde3146e"),
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("cd58bee4-f11a-48d3-bb38-098cda21ca0e"),
        patientUuid = patientWithFiveDayOverdue,
        scheduledDate = now.minusDays(5),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("16cbb757-2b66-4770-bcfc-1084499465d2"),
        patientUuid = patientWithTenDaysOverdue,
        scheduledDate = now.minusDays(10),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility2Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("11458119-82aa-4d52-998e-61d8562179d7"),
        patientUuid = patientWithFifteenDaysOverdue,
        scheduledDate = now.minusDays(15),
        remindOn = null,
        facilityUuid = facility2Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("4cf067b1-ba91-4cdd-a178-04e112c3b55a"),
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("d6c74eb5-e506-4bf1-a8e0-d1ec6b871c98"),
        patientUuid = deadPatientWithOverdue,
        scheduledDate = now.minusDays(7),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Dead,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("c20e2821-e761-458c-b8d3-1449866d305e"),
        patientUuid = patientWithVisitedAppointment,
        scheduledDate = now.minusDays(30),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("a1252589-b866-4386-afcb-f3a4ac3306a3"),
        patientUuid = patientWithCancelledAppointment,
        scheduledDate = now.minusDays(31),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Cancelled,
        callResult = TestData.callResult(
            removeReason = PatientNotResponding,
            outcome = RemovedFromOverdueList
        )
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("bd6239b2-914c-4414-8ae3-dae2583097c1"),
        patientUuid = patientWithAppointmentRemindedInFiveDaysSinceOverdue,
        scheduledDate = now.minusDays(2),
        remindOn = now.plusDays(7),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            outcome = RemindToCallLater
        )
    )

    //when
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacilityNew(
        since = now,
        facilityId = facility1Uuid
    ).blockingFirst().map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(
        patientWithOneDayOverdue,
        patientWithAppointmentRemindedInFiveDaysSinceOverdue,
        patientWithFiveDayOverdue,
        patientWithFifteenDaysOverdue,
        patientWithCancelledAppointment,
        patientWithOverAnYearDaysOverdue
    ))
  }

  @Test
  fun getting_pending_overdue_appointments_in_a_facility_should_work_correctly() {
    fun createOverdueAppointment(
        appointmentUuid: UUID,
        patientUuid: UUID,
        scheduledDate: LocalDate,
        remindOn: LocalDate?,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentStatus: Status,
        callResult: CallResult?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid,
          patientStatus = patientStatus
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = appointmentStatus,
          cancelReason = null,
          remindOn = remindOn
      )
      appointmentRepository.save(listOf(appointment))

      if (callResult != null) {
        callResultRepository.save(listOf(TestData.callResult(
            appointmentId = appointmentUuid,
            outcome = callResult.outcome,
            removeReason = callResult.removeReason
        )))
      }
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("f511c03d-8dcd-4704-b0cc-a255776be576")
    val patientWithFiveDayOverdue = UUID.fromString("7536e587-d499-4a1c-9e72-99271576c6e2")
    val patientWithTenDaysOverdue = UUID.fromString("431425a6-3a92-4824-a293-d7b58a9234a3")
    val patientWithFifteenDaysOverdue = UUID.fromString("bacf8ac2-a263-4f8d-ac52-a874b6060d80")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("ab08f06b-4dae-4839-af7a-11ef58d8b5dc")
    val deadPatientWithOverdue = UUID.fromString("3d517d89-67d4-420f-be08-d5eb7b214696")
    val patientWithVisitedAppointment = UUID.fromString("cf7b91ec-6f38-4143-a3b3-d4d95fe248de")
    val patientWithCancelledAppointment = UUID.fromString("30838edb-4889-40a1-b320-fc744d79b0de")
    val patientWithAppointmentRemindedInFiveDaysSinceOverdue = UUID.fromString("0142584d-6fb5-4086-9c19-4bd7e863f152")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("08f0d831-3d27-4e99-9404-b9d982ab3541")
    val facility2Uuid = UUID.fromString("0cdab726-8b44-4775-9158-8637329f2ad3")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("13f868ba-feaa-4cb6-8ea7-c4f4fde3146e"),
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("cd58bee4-f11a-48d3-bb38-098cda21ca0e"),
        patientUuid = patientWithFiveDayOverdue,
        scheduledDate = now.minusDays(5),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("16cbb757-2b66-4770-bcfc-1084499465d2"),
        patientUuid = patientWithTenDaysOverdue,
        scheduledDate = now.minusDays(10),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility2Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("11458119-82aa-4d52-998e-61d8562179d7"),
        patientUuid = patientWithFifteenDaysOverdue,
        scheduledDate = now.minusDays(15),
        remindOn = null,
        facilityUuid = facility2Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("4cf067b1-ba91-4cdd-a178-04e112c3b55a"),
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("d6c74eb5-e506-4bf1-a8e0-d1ec6b871c98"),
        patientUuid = deadPatientWithOverdue,
        scheduledDate = now.minusDays(7),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Dead,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("c20e2821-e761-458c-b8d3-1449866d305e"),
        patientUuid = patientWithVisitedAppointment,
        scheduledDate = now.minusDays(30),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("a1252589-b866-4386-afcb-f3a4ac3306a3"),
        patientUuid = patientWithCancelledAppointment,
        scheduledDate = now.minusDays(31),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Cancelled,
        callResult = TestData.callResult(
            removeReason = PatientNotResponding,
            outcome = RemovedFromOverdueList
        )
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("bd6239b2-914c-4414-8ae3-dae2583097c1"),
        patientUuid = patientWithAppointmentRemindedInFiveDaysSinceOverdue,
        scheduledDate = now.minusDays(2),
        remindOn = now.plusDays(7),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            outcome = RemindToCallLater
        )
    )

    //when
    val overdueAppointments = PagingTestCase(
        pagingSource = appointmentRepository.pendingOverdueAppointmentsInFacility(
            since = now,
            facilityId = facility1Uuid
        ),
        loadSize = 10
    ).loadPage()
        .data
        .map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(
        patientWithOneDayOverdue,
        patientWithFiveDayOverdue,
        patientWithFifteenDaysOverdue
    ))
  }

  @Test
  fun getting_agreed_to_visit_overdue_appointments_in_a_facility_should_work_correctly() {
    fun createOverdueAppointment(
        appointmentUuid: UUID,
        patientUuid: UUID,
        scheduledDate: LocalDate,
        remindOn: LocalDate?,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentStatus: Status,
        callResult: CallResult?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid,
          patientStatus = patientStatus
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = appointmentStatus,
          cancelReason = null,
          remindOn = remindOn
      )
      appointmentRepository.save(listOf(appointment))

      if (callResult != null) {
        callResultRepository.save(listOf(callResult))
      }
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("92eb73f2-7d15-4845-8977-6a45c62816e7")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("287558a9-3a18-49fb-8858-2bdb040412ed")
    val deadPatientWithOverdue = UUID.fromString("fcab66bd-5e6d-4f81-9a12-c66c3db7820a")
    val patientWithVisitedAppointment = UUID.fromString("0aa86238-51d9-4356-86cb-270b509d152d")
    val patientWithAgreedToVisitOverdueAppointment = UUID.fromString("8e51ec15-7c04-4b60-8609-fb4f71f03bdd")
    val patientWithAgreedToVisitOverdueAppointment2 = UUID.fromString("454df18a-fb58-4b3e-bf85-382ac5e019c5")

    val agreedToVisitAppointment = UUID.fromString("878385eb-dfaf-4275-a03f-acb7d80edac9")
    val agreedToVisitAppointment2 = UUID.fromString("88cfbc6e-8a00-4d36-825e-1393b65f6d24")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("9759d6e0-fd4e-4b97-b43d-2099dbcb0841")
    val facility2Uuid = UUID.fromString("0426f652-5203-484a-9ed7-c3adcbbd9f1d")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("cbd55cfb-9e15-4685-b698-63dae67ae55c"),
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("a2ee7bea-a519-445d-a51b-d37bd293f494"),
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("0c43de19-ea9b-4f5a-ad6c-f53493b6025f"),
        patientUuid = deadPatientWithOverdue,
        scheduledDate = now.minusDays(7),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Dead,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("8a1463e0-5876-48ec-beaf-93723f42bbb7"),
        patientUuid = patientWithVisitedAppointment,
        scheduledDate = now.minusDays(30),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = agreedToVisitAppointment,
        patientUuid = patientWithAgreedToVisitOverdueAppointment,
        scheduledDate = now.minusDays(2),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("0d739533-0934-4031-9ff3-3bce3ef375df"),
            outcome = AgreedToVisit,
            appointmentId = agreedToVisitAppointment
        )
    )
    createOverdueAppointment(
        appointmentUuid = agreedToVisitAppointment2,
        patientUuid = patientWithAgreedToVisitOverdueAppointment2,
        scheduledDate = now.minusDays(10),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("8777b60b-4081-4114-b598-26d45762bb45"),
            outcome = AgreedToVisit,
            appointmentId = agreedToVisitAppointment2
        )
    )

    //when
    val overdueAppointments = PagingTestCase(
        pagingSource = appointmentRepository.agreedToVisitOverdueAppointmentsInFacility(
            since = now,
            facilityId = facility1Uuid
        ),
        loadSize = 10
    ).loadPage()
        .data
        .map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(
        patientWithAgreedToVisitOverdueAppointment,
        patientWithAgreedToVisitOverdueAppointment2
    ))
  }

  @Test
  fun getting_remind_to_call_later_overdue_appointments_in_a_facility_should_work_correctly() {
    fun createOverdueAppointment(
        appointmentUuid: UUID,
        patientUuid: UUID,
        scheduledDate: LocalDate,
        remindOn: LocalDate?,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentStatus: Status,
        callResult: CallResult?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid,
          patientStatus = patientStatus
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = appointmentStatus,
          cancelReason = null,
          remindOn = remindOn
      )
      appointmentRepository.save(listOf(appointment))

      if (callResult != null) {
        callResultRepository.save(listOf(callResult))
      }
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("92eb73f2-7d15-4845-8977-6a45c62816e7")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("287558a9-3a18-49fb-8858-2bdb040412ed")
    val deadPatientWithOverdue = UUID.fromString("fcab66bd-5e6d-4f81-9a12-c66c3db7820a")
    val patientWithVisitedAppointment = UUID.fromString("0aa86238-51d9-4356-86cb-270b509d152d")
    val patientWithAgreedToVisitOverdueAppointment = UUID.fromString("99628f72-29dc-4746-9f10-ec9b2cffeaed")
    val patientWithAppointmentRemindedInFiveDaysSinceOverdue = UUID.fromString("5e84d754-3a1b-4014-856d-e6f641173407")
    val patientWithAppointmentRemindedInTenDaysSinceOverdue = UUID.fromString("82059663-b9ff-4e56-a771-1781e70f1085")

    val agreedToVisitAppointment = UUID.fromString("7c8ad2d9-2f43-4ab2-9638-263fc82d65ad")
    val remindToCallLaterAppointmentInFiveDays = UUID.fromString("67da29f8-9d44-4996-a99d-9e8a41cdd783")
    val remindToCallLaterAppointmentInTenDays = UUID.fromString("2a10da30-780c-455e-afaa-e8ec8a6d5cb2")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("9759d6e0-fd4e-4b97-b43d-2099dbcb0841")
    val facility2Uuid = UUID.fromString("0426f652-5203-484a-9ed7-c3adcbbd9f1d")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("cbd55cfb-9e15-4685-b698-63dae67ae55c"),
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("a2ee7bea-a519-445d-a51b-d37bd293f494"),
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("0c43de19-ea9b-4f5a-ad6c-f53493b6025f"),
        patientUuid = deadPatientWithOverdue,
        scheduledDate = now.minusDays(7),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Dead,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("8a1463e0-5876-48ec-beaf-93723f42bbb7"),
        patientUuid = patientWithVisitedAppointment,
        scheduledDate = now.minusDays(30),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = agreedToVisitAppointment,
        patientUuid = patientWithAgreedToVisitOverdueAppointment,
        scheduledDate = now.minusDays(2),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("0d739533-0934-4031-9ff3-3bce3ef375df"),
            outcome = AgreedToVisit,
            appointmentId = agreedToVisitAppointment
        )
    )
    createOverdueAppointment(
        appointmentUuid = remindToCallLaterAppointmentInFiveDays,
        patientUuid = patientWithAppointmentRemindedInFiveDaysSinceOverdue,
        scheduledDate = now.minusDays(2),
        remindOn = now.plusDays(5),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("3e84a0a7-a4ab-4c88-ba8b-5dc0072fb9f4"),
            outcome = RemindToCallLater,
            appointmentId = remindToCallLaterAppointmentInFiveDays
        )
    )
    createOverdueAppointment(
        appointmentUuid = remindToCallLaterAppointmentInTenDays,
        patientUuid = patientWithAppointmentRemindedInTenDaysSinceOverdue,
        scheduledDate = now.minusDays(2),
        remindOn = now.plusDays(10),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("263605e9-0f38-4872-b54b-c5ff98a3e025"),
            outcome = RemindToCallLater,
            appointmentId = remindToCallLaterAppointmentInTenDays
        )
    )

    //when
    val overdueAppointments = PagingTestCase(
        pagingSource = appointmentRepository.remindToCallLaterOverdueAppointmentsInFacility(
            since = now,
            facilityId = facility1Uuid
        ),
        loadSize = 10
    ).loadPage()
        .data
        .map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(
        patientWithAppointmentRemindedInFiveDaysSinceOverdue,
        patientWithAppointmentRemindedInTenDaysSinceOverdue
    ))
  }

  @Test
  fun getting_removed_overdue_appointments_in_a_facility_should_work_correctly() {
    fun createOverdueAppointment(
        appointmentUuid: UUID,
        patientUuid: UUID,
        scheduledDate: LocalDate,
        remindOn: LocalDate?,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentStatus: Status,
        callResult: CallResult?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid,
          patientStatus = patientStatus
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = appointmentStatus,
          cancelReason = null,
          remindOn = remindOn
      )
      appointmentRepository.save(listOf(appointment))

      if (callResult != null) {
        callResultRepository.save(listOf(callResult))
      }
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("534fb8ce-b072-413e-b76b-987cfbb267c5")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("af06bd89-b9e5-4234-a9fd-a9f5dbe0a4d9")
    val deadPatientWithOverdue = UUID.fromString("4878fe76-20ee-40d6-b617-185da96b92c0")
    val patientWithVisitedAppointment = UUID.fromString("10568adf-d63e-4ba3-adab-97386eb2ed7b")
    val patientWithAgreedToVisitOverdueAppointment = UUID.fromString("0df07e7b-081d-4afc-a2bc-73ee5d81be6f")
    val patientWithAppointmentRemindedInFiveDaysSinceOverdue = UUID.fromString("837daf69-d3b2-41b5-8047-6322914f768c")
    val patientWithRemovedOverdueAppointment = UUID.fromString("0bf27600-6499-4fa5-a33a-9dd5ee0567f1")

    val agreedToVisitAppointment = UUID.fromString("f2245e9a-bed3-47f0-a4ca-ea56a29654a4")
    val remindToCallLaterAppointmentInFiveDays = UUID.fromString("f57a2f92-95b7-4cbf-aa70-fa8014cad7f3")
    val removedOverdueAppointment = UUID.fromString("05daae45-2a65-42c5-90d6-877948b31326")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("9759d6e0-fd4e-4b97-b43d-2099dbcb0841")
    val facility2Uuid = UUID.fromString("0426f652-5203-484a-9ed7-c3adcbbd9f1d")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("cbd55cfb-9e15-4685-b698-63dae67ae55c"),
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("a2ee7bea-a519-445d-a51b-d37bd293f494"),
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("0c43de19-ea9b-4f5a-ad6c-f53493b6025f"),
        patientUuid = deadPatientWithOverdue,
        scheduledDate = now.minusDays(7),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Dead,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("8a1463e0-5876-48ec-beaf-93723f42bbb7"),
        patientUuid = patientWithVisitedAppointment,
        scheduledDate = now.minusDays(30),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = agreedToVisitAppointment,
        patientUuid = patientWithAgreedToVisitOverdueAppointment,
        scheduledDate = now.minusDays(2),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("0d739533-0934-4031-9ff3-3bce3ef375df"),
            outcome = AgreedToVisit,
            appointmentId = agreedToVisitAppointment
        )
    )
    createOverdueAppointment(
        appointmentUuid = remindToCallLaterAppointmentInFiveDays,
        patientUuid = patientWithAppointmentRemindedInFiveDaysSinceOverdue,
        scheduledDate = now.minusDays(2),
        remindOn = now.plusDays(5),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("3e84a0a7-a4ab-4c88-ba8b-5dc0072fb9f4"),
            outcome = RemindToCallLater,
            appointmentId = remindToCallLaterAppointmentInFiveDays
        )
    )
    createOverdueAppointment(
        appointmentUuid = removedOverdueAppointment,
        patientUuid = patientWithRemovedOverdueAppointment,
        scheduledDate = now.minusDays(2),
        remindOn = now.plusDays(10),
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = TestData.callResult(
            id = UUID.fromString("d288deab-3ca3-456f-bf66-db940deb2c35"),
            outcome = RemovedFromOverdueList,
            removeReason = PatientNotResponding,
            appointmentId = removedOverdueAppointment
        )
    )

    //when
    val overdueAppointments = PagingTestCase(
        pagingSource = appointmentRepository.removedOverdueAppointmentsInFacility(
            since = now,
            facilityId = facility1Uuid
        ),
        loadSize = 10
    ).loadPage()
        .data
        .map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(
        patientWithRemovedOverdueAppointment
    ))
  }

  @Test
  fun getting_overdue_appointments_that_are_more_than_an_year_in_a_facility_should_work_correctly() {
    fun createOverdueAppointment(
        appointmentUuid: UUID,
        patientUuid: UUID,
        scheduledDate: LocalDate,
        remindOn: LocalDate?,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentStatus: Status,
        callResult: CallResult?
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid,
          patientStatus = patientStatus
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = appointmentStatus,
          cancelReason = null,
          remindOn = remindOn
      )
      appointmentRepository.save(listOf(appointment))

      if (callResult != null) {
        callResultRepository.save(listOf(callResult))
      }
    }

    //given
    val patientWithOneDayOverdue = UUID.fromString("534fb8ce-b072-413e-b76b-987cfbb267c5")
    val deadPatientWithOverdue = UUID.fromString("4878fe76-20ee-40d6-b617-185da96b92c0")
    val patientWithVisitedAppointment = UUID.fromString("10568adf-d63e-4ba3-adab-97386eb2ed7b")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("af06bd89-b9e5-4234-a9fd-a9f5dbe0a4d9")
    val patientWithOverAnYearDaysOverdue2 = UUID.fromString("e4abce03-8383-44ca-96cc-52d78b6c9750")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("9759d6e0-fd4e-4b97-b43d-2099dbcb0841")
    val facility2Uuid = UUID.fromString("0426f652-5203-484a-9ed7-c3adcbbd9f1d")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("cbd55cfb-9e15-4685-b698-63dae67ae55c"),
        patientUuid = patientWithOneDayOverdue,
        scheduledDate = now.minusDays(1),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("0c43de19-ea9b-4f5a-ad6c-f53493b6025f"),
        patientUuid = deadPatientWithOverdue,
        scheduledDate = now.minusDays(7),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = facility1Uuid,
        patientStatus = Dead,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("8a1463e0-5876-48ec-beaf-93723f42bbb7"),
        patientUuid = patientWithVisitedAppointment,
        scheduledDate = now.minusDays(30),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("a2ee7bea-a519-445d-a51b-d37bd293f494"),
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(370),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("84e7332b-09f9-4c4a-b7fc-48a715954143"),
        patientUuid = patientWithOverAnYearDaysOverdue2,
        scheduledDate = now.minusDays(380),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Cancelled,
        callResult = TestData.callResult(
            id = UUID.fromString("26361ccd-0c03-4cd7-93fa-2e3094713078"),
            appointmentId = UUID.fromString("84e7332b-09f9-4c4a-b7fc-48a715954143"),
            outcome = RemovedFromOverdueList,
            removeReason = PatientNotResponding
        )
    )

    //when
    val overdueAppointments = PagingTestCase(
        pagingSource = appointmentRepository.moreThanAnYearOverduePatientInFacility(
            since = now,
            facilityId = facility1Uuid
        ),
        loadSize = 10
    ).loadPage()
        .data
        .map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(
        patientWithOverAnYearDaysOverdue,
        patientWithOverAnYearDaysOverdue2
    ))
  }

  @Test
  fun when_fetching_overdue_appointments_only_latest_appointments_that_are_not_visited_should_be_considered() {
    fun createOverdueAppointment(
        appointmentUuid: UUID,
        patientUuid: UUID,
        scheduledDate: LocalDate,
        remindOn: LocalDate?,
        facilityUuid: UUID,
        patientAssignedFacilityUuid: UUID?,
        patientStatus: PatientStatus,
        appointmentStatus: Status,
        callResult: CallResult?,
        appointmentCreatedAt: Instant
    ) {
      val patientProfile = TestData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAssignedFacilityId = patientAssignedFacilityUuid,
          patientStatus = patientStatus
      )
      patientRepository.save(listOf(patientProfile))

      val bp = TestData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          systolic = 120,
          diastolic = 80
      )
      bpRepository.save(listOf(bp))

      val bloodSugar = TestData.bloodSugarMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          reading = BloodSugarReading.fromMg("100", Random)
      )
      bloodSugarRepository.save(listOf(bloodSugar))

      val appointment = TestData.appointment(
          uuid = appointmentUuid,
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = scheduledDate,
          status = appointmentStatus,
          cancelReason = null,
          remindOn = remindOn,
          createdAt = appointmentCreatedAt
      )
      appointmentRepository.save(listOf(appointment))

      if (callResult != null) {
        callResultRepository.save(listOf(TestData.callResult(
            appointmentId = appointmentUuid,
            outcome = callResult.outcome,
            removeReason = callResult.removeReason
        )))
      }
    }

    //given
    val patientOne = UUID.fromString("ba25f40c-5d9c-4163-a834-0305f841a978")
    val patientTwo = UUID.fromString("9d447143-6acc-4475-a2a7-9a60e9f39271")

    val now = LocalDate.now(clock)
    val facilityUuid = UUID.fromString("08f0d831-3d27-4e99-9404-b9d982ab3541")
    val facility = TestData.facility(uuid = facilityUuid, name = "PHC Obvious")

    facilityRepository.save(listOf(facility))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("40db60d8-855b-40c4-915b-c515d50a5894"),
        patientUuid = patientOne,
        scheduledDate = now.minusMonths(1),
        remindOn = null,
        facilityUuid = facilityUuid,
        patientAssignedFacilityUuid = facilityUuid,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null,
        appointmentCreatedAt = Instant.parse("2017-11-12T00:00:00Z")
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("51197cc5-6a10-4607-b782-036ff8810647"),
        patientUuid = patientOne,
        scheduledDate = now.minusDays(2),
        remindOn = null,
        facilityUuid = facilityUuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null,
        appointmentCreatedAt = Instant.parse("2017-12-12T00:00:00Z")
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("1146fa98-73e4-46ea-b6a5-34feed863952"),
        patientUuid = patientTwo,
        scheduledDate = now.minusMonths(3),
        remindOn = null,
        facilityUuid = facilityUuid,
        patientAssignedFacilityUuid = facilityUuid,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null,
        appointmentCreatedAt = Instant.parse("2017-10-12T00:00:00Z")
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("7bd6fee8-dc21-47c7-b6b2-5c648c75ca8f"),
        patientUuid = patientTwo,
        scheduledDate = now.minusMonths(2),
        remindOn = null,
        facilityUuid = facilityUuid,
        patientAssignedFacilityUuid = facilityUuid,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null,
        appointmentCreatedAt = Instant.parse("2017-11-12T00:00:00Z")
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("be7747c8-e966-460e-bc0c-b9b841e020e7"),
        patientUuid = patientTwo,
        scheduledDate = now.minusDays(10),
        remindOn = null,
        facilityUuid = facilityUuid,
        patientAssignedFacilityUuid = facilityUuid,
        patientStatus = Active,
        appointmentStatus = Visited,
        callResult = null,
        appointmentCreatedAt = Instant.parse("2017-12-12T00:00:00Z")
    )

    //when
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacilityNew(
        since = now,
        facilityId = facilityUuid
    ).blockingFirst().map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(patientOne))
  }

  @Suppress("LocalVariableName")
  @Test
  fun fetching_the_latest_overdue_appointment_for_a_patient_should_account_for_cancelled_appointments() {
    // given
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        generatePhoneNumber = true
    )
    patientRepository.save(listOf(patientProfile))

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

    val appointment_cancelled = TestData.appointment(
        uuid = UUID.fromString("764a4cf6-f09e-40c2-814b-f3283a49fac2"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        status = Cancelled,
        scheduledDate = aWeekInFuture,
        remindOn = aWeekInFuture.plusDays(1),
        createdAt = aWeekInFuture.toUtcInstant(userClock),
        updatedAt = aWeekInFuture.toUtcInstant(userClock),
        deletedAt = null
    )

    bpRepository.save(listOf(
        bp_recorded_a_week_ago
    ))

    appointmentRepository.save(listOf(
        appointment_cancelled
    ))

    // then
    val cancelled_appointment = appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, aWeekInFuture.plusDays(1))
    assertThat(cancelled_appointment.get()).isEqualTo(appointment_cancelled)
  }

  @Test
  fun fetching_appointments_and_patient_information_for_given_ids_should_work_as_expected() {
    // given
    val patient1Uuid = UUID.fromString("714ea934-03c5-4d5f-9982-4f3b9f297622")
    val patient2Uuid = UUID.fromString("4788154c-ebf3-4c0e-9141-668896351edc")
    val patient3Uuid = UUID.fromString("73a2d3f8-1ca4-4c5a-9e6e-a3b99142643d")

    val patient1 = TestData.patientProfile(
        patientUuid = patient1Uuid,
        generatePhoneNumber = false,
        patientPhoneNumber = "1111111111",
        generateBusinessId = false,
        businessId = TestData.businessId(
            patientUuid = patient1Uuid,
            identifier = Identifier(
                value = "773d3caf-91f8-47f0-8766-45cf9cefd743",
                type = BpPassport
            )
        ),
        gender = Gender.Male,
        patientName = "Ramesh Murthy",
        patientAgeDetails = PatientAgeDetails(
            ageValue = 47,
            ageUpdatedAt = Instant.parse("2017-02-01T00:00:00Z"),
            dateOfBirth = null
        ),
        patientCreatedAt = Instant.parse("2017-02-01T00:00:00Z"),
        patientAddressStreet = "Achalesvara Square",
        patientAddressColonyOrVillage = "821 Susheel Row"
    )
    val patient2 = TestData.patientProfile(patientUuid = patient2Uuid)
    val patient3 = TestData.patientProfile(
        patientUuid = patient3Uuid,
        generatePhoneNumber = false,
        patientPhoneNumber = "2222222222",
        generateBusinessId = false,
        businessId = TestData.businessId(
            patientUuid = patient3Uuid,
            identifier = Identifier(
                value = "08b2a0fb-fe5d-47fd-ad73-2a8b594a3621",
                type = BpPassport
            )
        ),
        gender = Gender.Female,
        patientName = "Shreya Mishra",
        patientAgeDetails = PatientAgeDetails(
            ageValue = null,
            ageUpdatedAt = Instant.parse("2017-01-01T00:00:00Z"),
            dateOfBirth = LocalDate.parse("1987-01-01")
        ),
        patientCreatedAt = Instant.parse("2017-01-01T00:00:00Z"),
        patientAddressStreet = "Marar Mountain",
        patientAddressColonyOrVillage = "95906 Pillai Plaza"
    )

    val appointmentForPatient1UUID = UUID.fromString("13ad6674-ab3a-454a-aa12-a827cef374b0")
    val appointmentForPatient2UUID = UUID.fromString("01472302-001d-4ac5-b93e-9477e3a09032")
    val appointmentForPatient3UUID = UUID.fromString("a6ec46c1-2715-41fb-a60e-d5ccb8851526")

    val appointmentForPatient1 = TestData.appointment(
        uuid = appointmentForPatient1UUID,
        patientUuid = patient1Uuid,
        status = Scheduled,
        scheduledDate = LocalDate.parse("2018-01-07"),
        cancelReason = null,
        appointmentType = Manual,
        syncStatus = DONE
    )
    val appointmentForPatient2 = TestData.appointment(
        uuid = appointmentForPatient2UUID,
        patientUuid = patient2Uuid,
        status = Cancelled,
        cancelReason = PatientNotResponding,
        appointmentType = Manual,
        syncStatus = DONE
    )
    val appointmentForPatient3 = TestData.appointment(
        uuid = appointmentForPatient3UUID,
        patientUuid = patient3Uuid,
        status = Cancelled,
        scheduledDate = LocalDate.parse("2018-02-01"),
        cancelReason = InvalidPhoneNumber,
        appointmentType = Automatic,
        syncStatus = PENDING
    )

    val prescribedDrugsForPatient1 = listOf(
        TestData.prescription(
            uuid = UUID.fromString("96a7c808-1f36-42db-8083-346f7b8726d3"),
            name = "Amlodipine",
            dosage = "25 mg",
            isDeleted = false,
            patientUuid = patient1Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("31d1c637-21dc-4453-8c78-10c1a5405523"),
            name = "Atenolol",
            dosage = "25 mg",
            isDeleted = false,
            patientUuid = patient1Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("e2350bf8-c2ab-4461-9708-f00cf2133453"),
            name = "Aspirin",
            dosage = "15 mg",
            isDeleted = true,
            patientUuid = patient1Uuid
        )
    )

    val prescribedDrugsForPatient2 = listOf(
        TestData.prescription(
            uuid = UUID.fromString("cdc5569d-2572-4753-b5c2-951d13a1a84a"),
            patientUuid = patient2Uuid,
            isDeleted = false
        )
    )

    val prescribedDrugsForPatient3 = listOf(
        TestData.prescription(
            uuid = UUID.fromString("7766017c-719e-4e12-b64a-3870f7a3b81b"),
            name = "Captopril",
            dosage = "10 mg",
            isDeleted = false,
            patientUuid = patient3Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("dcbf71ce-115d-4d5d-b4fd-10a292b92573"),
            name = "Temisartan",
            dosage = "15 mg",
            isDeleted = false,
            patientUuid = patient3Uuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("41468679-44b0-4170-a282-55431b33f8d8"),
            name = "Aspirin",
            dosage = "15 mg",
            isDeleted = true,
            patientUuid = patient3Uuid
        )
    )

    patientRepository.save(listOf(patient1, patient2, patient3))
    appointmentRepository.save(listOf(appointmentForPatient1, appointmentForPatient2, appointmentForPatient3))
    prescriptionRepository.save(prescribedDrugsForPatient1 + prescribedDrugsForPatient2 + prescribedDrugsForPatient3)

    // when
    val appointmentsCursor = appointmentRepository.appointmentAndPatientInformationForIds(listOf(appointmentForPatient1UUID, appointmentForPatient3UUID))

    // then
    assertThat(appointmentsCursor.count).isEqualTo(2)
    assertThat(appointmentsCursor.moveToNext()).isTrue()
    appointmentsCursor.assertValues(mapOf(
        "patientCreatedAt" to Instant.parse("2017-02-01T00:00:00Z"),
        "identifierValue" to "773d3caf-91f8-47f0-8766-45cf9cefd743",
        "patientName" to "Ramesh Murthy",
        "patientGender" to "male",
        "patientAgeValue" to "47",
        "patientAgeUpdatedAt" to Instant.parse("2017-02-01T00:00:00Z"),
        "patientDateOfBirth" to null,
        "patientStreetAddress" to "Achalesvara Square",
        "patientColonyOrVillage" to "821 Susheel Row",
        "appointmentScheduledAt" to LocalDate.parse("2018-01-07"),
        "patientPhoneNumber" to "1111111111",
        "prescribedDrugs" to "Amlodipine 25 mg, Atenolol 25 mg"
    ))
    assertThat(appointmentsCursor.moveToNext()).isTrue()
    appointmentsCursor.assertValues(mapOf(
        "patientCreatedAt" to Instant.parse("2017-01-01T00:00:00Z"),
        "identifierValue" to "08b2a0fb-fe5d-47fd-ad73-2a8b594a3621",
        "patientName" to "Shreya Mishra",
        "patientGender" to "female",
        "patientAgeValue" to null,
        "patientAgeUpdatedAt" to Instant.parse("2017-01-01T00:00:00Z"),
        "patientDateOfBirth" to LocalDate.parse("1987-01-01"),
        "patientStreetAddress" to "Marar Mountain",
        "patientColonyOrVillage" to "95906 Pillai Plaza",
        "appointmentScheduledAt" to LocalDate.parse("2018-02-01"),
        "patientPhoneNumber" to "2222222222",
        "prescribedDrugs" to "Captopril 10 mg, Temisartan 15 mg"
    ))
    assertThat(appointmentsCursor.moveToNext()).isFalse()
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
      patientRepository.save(listOf(patientProfile))
      bloodPressureRepository.save(listOfNotNull(bloodPressureMeasurement))
      bloodSugarRepository.save(listOfNotNull(bloodSugarMeasurement))
      appointmentRepository.save(listOf(appointment))
    }

    fun toOverdueAppointment(appointmentFacilityName: String?, registeredFacilityName: String?): OverdueAppointment_Old {
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
        return OverdueAppointment_Old(
            fullName = patientProfile.patient.fullName,
            gender = patientProfile.patient.gender,
            ageDetails = patientProfile.patient.ageDetails,
            appointment = appointment,
            phoneNumber = patientProfile.phoneNumbers.firstOrNull(),
            patientAddress = overduePatientAddress,
            isAtHighRisk = false,
            patientAssignedFacilityUuid = patientProfile.patient.assignedFacilityId
        )
      }
    }
  }

  @Test
  fun searching_for_overdue_appointments_should_work_correctly() {
    fun createOverdueAppointment(
        patientName: String,
        colonyOrVillageName: String,
        patientAddressUuid: UUID,
        patientUuid: UUID,
        facilityUuid: UUID
    ) {
      val patientAddress = testData.patientAddress(
          uuid = patientAddressUuid,
          colonyOrVillage = colonyOrVillageName
      )

      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAddressUuid = patientAddressUuid,
          patientName = patientName
      )

      patientRepository.save(listOf(patientProfile))
      database.addressDao().save(listOf(patientAddress))

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp))

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = LocalDate.now().minusDays(10),
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment))
    }

    // given
    val searchInputs = listOf("bar")
    val facilityUuid = UUID.fromString("722111d2-459c-47a6-859c-ca3564fc374d")

    val patient1WithMatchingName = UUID.fromString("7c7a09ce-d5b7-4c75-9983-01704d8e9cc6")
    val patient2WithMatchingName = UUID.fromString("385cf640-ba6d-4fac-8098-612e2620643b")
    val patientWithMatchingVillageName = UUID.fromString("0d165e45-d12d-4a4a-bbc4-5f01a7955486")
    val patientWithNothingMatching = UUID.fromString("d2046ee2-8474-4202-989d-1f2eac2deb2a")

    createOverdueAppointment(
        patientName = "Barminder",
        colonyOrVillageName = "CHC Bucha",
        patientAddressUuid = UUID.fromString("a78fed33-db60-4cbc-b721-e487774be9e6"),
        patientUuid = patient1WithMatchingName,
        facilityUuid = facilityUuid
    )
    createOverdueAppointment(
        patientName = "Misthi Barish",
        colonyOrVillageName = "CHC Bathinda",
        patientAddressUuid = UUID.fromString("7af1fd3a-b307-4b60-9b2a-e2c4188a73c4"),
        patientUuid = patient2WithMatchingName,
        facilityUuid = facilityUuid
    )
    createOverdueAppointment(
        patientName = "Riya Mukherjee",
        colonyOrVillageName = "CHC Barmia",
        patientAddressUuid = UUID.fromString("3a563a7b-13d4-4a3a-a03c-651c33167216"),
        patientUuid = patientWithMatchingVillageName,
        facilityUuid = facilityUuid
    )
    createOverdueAppointment(
        patientName = "Riya Mukherjee",
        colonyOrVillageName = "CHC Bhatinda",
        patientAddressUuid = UUID.fromString("0bc53ae9-cbc4-4076-901a-b915d4fd2cef"),
        patientUuid = patientWithNothingMatching,
        facilityUuid = facilityUuid
    )

    // when
    val overduePatientUuids = PagingTestCase(pagingSource = appointmentRepository.searchOverduePatient(
        searchInputs = searchInputs,
        since = LocalDate.now(),
        facilityId = facilityUuid),
        loadSize = 10)
        .loadPage()
        .data
        .map { it.appointment.patientUuid }

    // then
    assertThat(overduePatientUuids).containsExactly(
        patient1WithMatchingName,
        patient2WithMatchingName,
        patientWithMatchingVillageName
    )
    assertThat(overduePatientUuids).doesNotContain(
        patientWithNothingMatching
    )
  }

  @Test
  fun searching_for_overdue_appointments_with_multiple_search_inputs_should_work_correctly() {
    fun createOverdueAppointment(
        patientName: String,
        colonyOrVillageName: String,
        patientAddressUuid: UUID,
        patientUuid: UUID,
        facilityUuid: UUID
    ) {
      val patientAddress = testData.patientAddress(
          uuid = patientAddressUuid,
          colonyOrVillage = colonyOrVillageName
      )

      val patientProfile = testData.patientProfile(
          patientUuid = patientUuid,
          generatePhoneNumber = true,
          patientAddressUuid = patientAddressUuid,
          patientName = patientName
      )

      patientRepository.save(listOf(patientProfile))
      database.addressDao().save(listOf(patientAddress))

      val bp = testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid
      )
      bpRepository.save(listOf(bp))

      val appointment = testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = facilityUuid,
          scheduledDate = LocalDate.now().minusDays(10),
          status = Scheduled,
          cancelReason = null
      )
      appointmentRepository.save(listOf(appointment))
    }

    // given
    val searchInputs = listOf("Babri", "Narwar", "Anand Krishna", "Shreya")
    val facilityUuid = UUID.fromString("6c047cfe-2341-4087-921f-20743a088e0f")

    val patientWithMatchingName = UUID.fromString("e78e9d20-9806-4de7-849b-899e6233466b")
    val patient1WithMatchingVillageName = UUID.fromString("9fb31349-b26a-4f9b-b032-7025c68b76a3")
    val patient2WithMatchingVillageName = UUID.fromString("719530a3-c4b3-43df-b8ee-ecfc3dc8e47e")
    val patient3WithMatchingVillageName = UUID.fromString("d2554cd9-14d3-4e10-9608-f1e3573e9064")
    val patientWithNothingMatching = UUID.fromString("8210223a-fb44-46bf-beed-448a33b65e85")

    createOverdueAppointment(
        patientName = "Anand Krishna",
        colonyOrVillageName = "Prakash Nagar",
        patientAddressUuid = UUID.fromString("a78fed33-db60-4cbc-b721-e487774be9e6"),
        patientUuid = patientWithMatchingName,
        facilityUuid = facilityUuid
    )
    createOverdueAppointment(
        patientName = "Misthi Barish",
        colonyOrVillageName = "Babri",
        patientAddressUuid = UUID.fromString("7af1fd3a-b307-4b60-9b2a-e2c4188a73c4"),
        patientUuid = patient1WithMatchingVillageName,
        facilityUuid = facilityUuid
    )
    createOverdueAppointment(
        patientName = "Riya Mukherjee",
        colonyOrVillageName = "Narwar",
        patientAddressUuid = UUID.fromString("3a563a7b-13d4-4a3a-a03c-651c33167216"),
        patientUuid = patient2WithMatchingVillageName,
        facilityUuid = facilityUuid
    )
    createOverdueAppointment(
        patientName = "Priya Mukherjee",
        colonyOrVillageName = "Narwar",
        patientAddressUuid = UUID.fromString("3a563a7b-13d4-4a3a-a03c-651c33167216"),
        patientUuid = patient3WithMatchingVillageName,
        facilityUuid = facilityUuid
    )
    createOverdueAppointment(
        patientName = "Riya Mukherjee",
        colonyOrVillageName = "CHC Bhatinda",
        patientAddressUuid = UUID.fromString("0bc53ae9-cbc4-4076-901a-b915d4fd2cef"),
        patientUuid = patientWithNothingMatching,
        facilityUuid = facilityUuid
    )

    // when
    val overduePatientUuids = PagingTestCase(pagingSource = appointmentRepository.searchOverduePatient(
        searchInputs = searchInputs,
        since = LocalDate.now(),
        facilityId = facilityUuid),
        loadSize = 10)
        .loadPage()
        .data
        .map { it.appointment.patientUuid }

    // then
    assertThat(overduePatientUuids).containsExactly(
        patientWithMatchingName,
        patient1WithMatchingVillageName,
        patient2WithMatchingVillageName,
        patient3WithMatchingVillageName
    )
    assertThat(overduePatientUuids).doesNotContain(
        patientWithNothingMatching
    )
  }
}

private fun PatientPhoneNumber.withPatientUuid(uuid: UUID): PatientPhoneNumber {
  return this.copy(patientUuid = uuid)
}
