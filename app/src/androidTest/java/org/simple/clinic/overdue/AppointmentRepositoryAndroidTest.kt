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
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.OverduePatientAddress
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
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

  private fun markAppointmentSyncStatusAsDone(vararg appointmentUuids: UUID) {
    appointmentRepository.setSyncStatus(appointmentUuids.toList(), DONE)
  }

  private fun getAppointmentByUuid(appointmentUuid: UUID): Appointment {
    return database.appointmentDao().getOne(appointmentUuid)!!
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
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacility(since = LocalDate.now(clock),
        facilityId = facility.uuid
    ).blockingFirst()

    //then
    assertThat(overdueAppointments).hasSize(1)
    assertThat(overdueAppointments.first().appointment.patientUuid).isEqualTo(notDeletedPatientId)
  }

  @Test
  fun dead_patients_must_be_excluded_when_loading_overdue_appointments() {
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
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacility(
        since = now,
        facilityId = facility2.uuid
    ).blockingFirst().map { it.appointment.uuid }

    //then
    assertThat(overdueAppointments).containsExactly(
        appointmentWithOneDayOverdue,
        appointmentWithTenDaysOverdue
    )
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
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacility(
        since = LocalDate.now(clock),
        facilityId = facility.uuid
    ).blockingFirst()

    //then
    assertThat(overdueAppointments).hasSize(1)
    assertThat(overdueAppointments.first().appointment.patientUuid).isEqualTo(patientIdWithoutDeletedAppointment)
  }

  @Test
  fun patients_without_phone_number_should_be_present_when_fetching_overdue_appointments() {

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

      return RecordAppointment(
          patientProfile = patientProfile,
          bloodPressureMeasurement = bloodPressureMeasurement,
          bloodSugarMeasurement = null,
          appointment = appointment,
          callResult = null
      )
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
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacility(
        since = currentDate,
        facilityId = facility.uuid
    ).blockingFirst()

    // then
    val expectedAppointments = listOf(withPhoneNumber, withoutPhoneNumber, withDeletedPhoneNumber).map { it.toOverdueAppointment() }

    assertThat(overdueAppointments).containsExactlyElementsIn(expectedAppointments)
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
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, fiftyNineSecondsLater)).isTrue()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()

    setAppointmentSyncStatusToDone(appointment1ForPatient.uuid)
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(appointmentForSomeOtherPatient.patientUuid, oneSecondEarlier)).isTrue()

    setAppointmentSyncStatusToDone(appointmentForSomeOtherPatient.uuid)
    assertThat(appointmentRepository.hasAppointmentForPatientChangedSince(appointmentForSomeOtherPatient.patientUuid, oneSecondEarlier)).isTrue()
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
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacility(
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
    val overdueAppointments = appointmentRepository.overdueAppointmentsInFacility(
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

  @Test
  fun fetching_more_than_a_year_overdue_appointments_in_a_facility_should_work_correctly() {
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
    val patientWithOneDayOverdue = UUID.fromString("9f9a670f-bcb4-41c3-ae6a-af545c251591")
    val patientWithFiveDayOverdue = UUID.fromString("1e961444-61dc-461c-995e-699fae86697b")
    val patientWithOverAnYearDaysOverdue = UUID.fromString("bb8726ba-6bae-4c0d-a542-0f16743049c7")
    val patientWithOverAnYearDaysOverdue2 = UUID.fromString("8ded6d89-b8a8-4183-bc59-ea5a25df83e4")
    val deadPatientWithOverdue = UUID.fromString("f2bf41d3-20ab-4f99-8e38-4228794f7293")
    val patientWithVisitedAppointment = UUID.fromString("176e8246-6d1d-4017-9771-152d8f2f2858")
    val patientWithCancelledAppointment = UUID.fromString("3686f790-63d8-4109-b169-329b468fad77")
    val patientWithAppointmentRemindedInFiveDaysSinceOverdue = UUID.fromString("767b9911-4ccf-4d68-9a02-baa38dc82cb4")

    val now = LocalDate.now(clock)
    val facility1Uuid = UUID.fromString("1f43b6d8-b318-4fae-8aa6-166f83b8d475")
    val facility2Uuid = UUID.fromString("39597424-3f79-4a11-997a-0d0f9588a15e")

    val facility1 = TestData.facility(uuid = facility1Uuid, name = "PHC Obvious")
    val facility2 = TestData.facility(uuid = facility2Uuid, name = "PHC Bagta")

    facilityRepository.save(listOf(facility1, facility2))

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("fe582055-7ad1-4a31-8fa4-f242d3abe336"),
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
        appointmentUuid = UUID.fromString("8178bce4-cb53-4a60-8d6a-c991a4ad55b7"),
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
        appointmentUuid = UUID.fromString("c7204185-c00c-46a5-b07f-0b9883e32e37"),
        patientUuid = patientWithOverAnYearDaysOverdue,
        scheduledDate = now.minusDays(366),
        remindOn = null,
        facilityUuid = facility1Uuid,
        patientAssignedFacilityUuid = null,
        patientStatus = Active,
        appointmentStatus = Scheduled,
        callResult = null
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("e0c54bf0-ecef-4781-9a98-43d5ff191729"),
        patientUuid = patientWithOverAnYearDaysOverdue2,
        scheduledDate = now.minusDays(380),
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
        scheduledDate = now.minusDays(400),
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
        scheduledDate = now.minusDays(375),
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
        scheduledDate = now.minusDays(382),
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
    val overdueAppointments = appointmentRepository.moreThanAnYearOverdueAppointments(
        since = now,
        facilityId = facility1Uuid
    ).blockingFirst().map { it.appointment.patientUuid }

    //then
    assertThat(overdueAppointments).isEqualTo(listOf(
        patientWithOverAnYearDaysOverdue,
        patientWithOverAnYearDaysOverdue2,
        patientWithCancelledAppointment
    ))
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

private data class RecordAppointment(
    val patientProfile: PatientProfile,
    val bloodPressureMeasurement: BloodPressureMeasurement?,
    val bloodSugarMeasurement: BloodSugarMeasurement?,
    val appointment: Appointment,
    val callResult: CallResult?
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

  fun toOverdueAppointment(): OverdueAppointment {
    if (bloodPressureMeasurement == null && bloodSugarMeasurement == null) {
      throw AssertionError("Need a Blood Pressure Measurement or Blood Sugar Measurement to create an Overdue Appointment")
    } else {
      val overduePatientAddress = OverduePatientAddress(
          streetAddress = patientProfile.address.streetAddress,
          colonyOrVillage = patientProfile.address.colonyOrVillage,
          district = patientProfile.address.district,
          state = patientProfile.address.state
      )
      return OverdueAppointment(
          fullName = patientProfile.patient.fullName,
          gender = patientProfile.patient.gender,
          ageDetails = patientProfile.patient.ageDetails,
          appointment = appointment,
          phoneNumber = patientProfile.phoneNumbers.firstOrNull(),
          patientAddress = overduePatientAddress,
          patientAssignedFacilityUuid = patientProfile.patient.assignedFacilityId,
          callResult = callResult
      )
    }
  }
}

private fun PatientPhoneNumber.withPatientUuid(uuid: UUID): PatientPhoneNumber {
  return this.copy(patientUuid = uuid)
}
