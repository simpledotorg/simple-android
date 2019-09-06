package org.simple.clinic.overdue

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.HIGH
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.HIGHEST
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.LOW
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.NONE
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.REGULAR
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.VERY_HIGH
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.Appointment.Status.Visited
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AppointmentRepositoryAndroidTest {

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var bpRepository: BloodPressureRepository

  @Inject
  lateinit var medicalHistoryRepository: MedicalHistoryRepository

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var faker: Faker

  @Inject
  lateinit var clock: TestUtcClock

  private val facility: Facility
    get() = testData.qaFacility()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(LocalAuthenticationRule())
      .around(RxErrorsRule())!!

  private val patientUuid = UUID.fromString("fcf0acd3-0b09-4ecb-bcd4-af40ca6456fc")
  private val appointmentUuid = UUID.fromString("a374e38f-6bc3-4829-899c-0966a4e13b10")

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.parse("2018-01-01"))
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }

  @Test
  fun when_creating_new_appointment_then_the_appointment_should_be_saved() {
    // given
    val appointmentDate = LocalDate.now(clock)

    //when
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentDate,
        appointmentType = Manual,
        currentFacility = facility
    ).blockingGet()

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
        currentFacility = facility
    ).blockingGet()
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
        currentFacility = facility
    ).blockingGet()

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
  fun when_fetching_appointments_then_only_return_overdue_appointments() {
    // given
    val address1 = UUID.fromString("bd3a0da9-99e2-49ef-b014-baff19de3cde")
    database.addressDao().save(
        PatientAddress(
            address1,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            Instant.now(clock),
            Instant.now(clock),
            null
        )
    )
    val patient1 = UUID.fromString("f4abcac4-c798-4b2d-a5f4-c85cea784916")
    val date1 = LocalDate.now(clock).minusDays(100)
    val bp1 = UUID.fromString("b9ed0a4a-ed7f-4805-bf2a-b06b53d7307f")
    database.patientDao().save(
        Patient(
            patient1,
            address1,
            faker.name.name(),
            Gender.Female,
            LocalDate.parse("1947-08-15"),
            null,
            PatientStatus.Active,
            Instant.now(clock),
            Instant.now(clock),
            null,
            Instant.now(clock),
            DONE)
    )
    database.bloodPressureDao().save(listOf(
        BloodPressureMeasurement(
            uuid = bp1,
            systolic = 190,
            diastolic = 100,
            syncStatus = PENDING,
            userUuid = testData.qaUserUuid(),
            facilityUuid = facility.uuid,
            patientUuid = patient1,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null,
            recordedAt = Instant.now(clock)
        )
    ))

    val patient2 = UUID.fromString("4850dc5a-dbf5-47df-942f-aac052bf95d2")
    val address2 = UUID.fromString("c7308677-3dd6-41b8-9645-a1fd58bcdb54")
    val phoneNumber2 = UUID.fromString("20bd3207-d0b3-4c36-91f4-c21c029a3109")
    database.addressDao().save(
        PatientAddress(
            address2,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            Instant.now(clock),
            Instant.now(clock),
            null
        )
    )
    database.patientDao().save(
        Patient(
            uuid = patient2,
            addressUuid = address2,
            fullName = faker.name.name(),
            gender = Gender.Transgender,
            dateOfBirth = LocalDate.parse("1997-08-15"),
            age = null,
            status = PatientStatus.Active,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null,
            recordedAt = Instant.now(clock),
            syncStatus = DONE
        )
    )
    database.phoneNumberDao().save(listOf(
        PatientPhoneNumber(
            uuid = phoneNumber2,
            patientUuid = patient2,
            number = "983374583",
            phoneType = PatientPhoneNumberType.Mobile,
            active = false,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null
        ))
    )

    val patient3 = UUID.fromString("6b8a2ad2-2be8-4ed8-add5-a2fe11c9f9fd")
    val address3 = UUID.fromString("4b619331-53c2-4de0-81ec-cf9217e88442")
    val phoneNumber3 = UUID.fromString("2b3b3439-624a-43fc-ae0c-8b5e7594dfcd")
    val date3 = LocalDate.now(clock).minusDays(10)
    val bp30 = UUID.fromString("223e1d52-41ac-4112-ba38-2797cc73c693")
    val bp31 = UUID.fromString("6822fc5f-b4fc-4524-a19a-2c162227e6d2")
    database.addressDao().save(
        PatientAddress(
            address3,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            Instant.now(clock),
            Instant.now(clock),
            null
        )
    )
    database.patientDao().save(
        Patient(
            patient3,
            address1,
            faker.name.name(),
            Gender.Male,
            LocalDate.parse("1977-11-15"),
            null,
            PatientStatus.Migrated,
            Instant.now(clock),
            Instant.now(clock),
            null,
            Instant.now(clock),
            DONE)
    )
    database.phoneNumberDao().save(listOf(
        PatientPhoneNumber(
            uuid = phoneNumber3,
            patientUuid = patient3,
            number = "983374583",
            phoneType = PatientPhoneNumberType.Mobile,
            active = true,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null
        ))
    )
    database.bloodPressureDao().save(listOf(
        BloodPressureMeasurement(
            uuid = bp30,
            systolic = 190,
            diastolic = 100,
            syncStatus = PENDING,
            userUuid = testData.qaUserUuid(),
            facilityUuid = facility.uuid,
            patientUuid = patient3,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null,
            recordedAt = Instant.now(clock)
        ),
        BloodPressureMeasurement(
            uuid = bp31,
            systolic = 180,
            diastolic = 110,
            syncStatus = PENDING,
            userUuid = testData.qaUserUuid(),
            facilityUuid = facility.uuid,
            patientUuid = patient3,
            createdAt = Instant.now(clock).minusSeconds(1000),
            updatedAt = Instant.now(clock).minusSeconds(1000),
            deletedAt = null,
            recordedAt = Instant.now(clock)
        )
    ))

    val scheduleAppointmentForPatient1 = appointmentRepository.schedule(
        patientUuid = patient1,
        appointmentUuid = UUID.fromString("5b78b730-8700-4d17-9aa9-25b443b10d81"),
        appointmentDate = date1,
        appointmentType = Manual,
        currentFacility = facility
    ).toCompletable()

    val scheduleAppointmentForPatient2 = appointmentRepository.schedule(
        patientUuid = patient2,
        appointmentUuid = UUID.fromString("ecc55e5b-f24f-46ff-9a16-6d922d1f180e"),
        appointmentDate = LocalDate.now(clock).minusDays(2),
        appointmentType = Manual,
        currentFacility = facility
    ).toCompletable()

    val scheduleAppointmentForPatient3 = appointmentRepository.schedule(
        patientUuid = patient3,
        appointmentUuid = UUID.fromString("ef63b0b4-8846-4bf4-bc1f-69996192e0c8"),
        appointmentDate = date3,
        appointmentType = Manual,
        currentFacility = facility
    ).toCompletable()

    scheduleAppointmentForPatient1
        .andThen(scheduleAppointmentForPatient2)
        .andThen(scheduleAppointmentForPatient3)
        .blockingGet()

    //when
    val overdueAppointments = appointmentRepository.overdueAppointments(facility).blockingFirst()

    // then
    assertThat(overdueAppointments).hasSize(1)

    with(overdueAppointments.first()) {
      assertThat(appointment.patientUuid).isEqualTo(patient3)
      assertThat(appointment.scheduledDate).isEqualTo(date3)
      assertThat(appointment.status).isEqualTo(Scheduled)
      assertThat(appointment.cancelReason).isEqualTo(null)
      assertThat(bloodPressure.uuid).isEqualTo(bp30)
    }
  }

  @Test
  fun deleted_blood_pressure_measurements_should_not_be_considered_when_fetching_overdue_appointments() {
    fun createBloodPressure(patientUuid: UUID, deletedAt: Instant? = null): BloodPressureMeasurement {
      return testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = facility.uuid,
          userUuid = testData.qaUserUuid(),
          syncStatus = DONE,
          createdAt = Instant.now(),
          updatedAt = Instant.now(),
          deletedAt = deletedAt)
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
        createBloodPressure(patientUuid = noBpsDeletedPatientUuid),
        createBloodPressure(patientUuid = noBpsDeletedPatientUuid)
    )

    val bpsForPatientWithLatestBpDeleted = listOf(
        createBloodPressure(patientUuid = latestBpDeletedPatientUuid),
        createBloodPressure(patientUuid = latestBpDeletedPatientUuid),
        createBloodPressure(patientUuid = latestBpDeletedPatientUuid, deletedAt = Instant.now(clock))
    )

    val bpsForPatientWithOldestBpNotDeleted = listOf(
        createBloodPressure(patientUuid = oldestBpNotDeletedPatientUuid),
        createBloodPressure(patientUuid = oldestBpNotDeletedPatientUuid, deletedAt = Instant.now(clock)),
        createBloodPressure(patientUuid = oldestBpNotDeletedPatientUuid, deletedAt = Instant.now(clock))
    )

    val bpsForPatientWithAllBpsDeleted = listOf(
        createBloodPressure(patientUuid = allBpsDeletedPatientUuid, deletedAt = Instant.now(clock)),
        createBloodPressure(patientUuid = allBpsDeletedPatientUuid, deletedAt = Instant.now(clock)),
        createBloodPressure(patientUuid = allBpsDeletedPatientUuid, deletedAt = Instant.now(clock))
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
    val overdueAppointments = appointmentRepository.overdueAppointments(facility).blockingFirst()
        .associateBy { it.appointment.patientUuid }

    // then
    assertThat(overdueAppointments.keys).containsExactly(noBpsDeletedPatientUuid, latestBpDeletedPatientUuid, oldestBpNotDeletedPatientUuid)

    val appointmentBpUuidOfNoBpsDeletedPatient = overdueAppointments.getValue(noBpsDeletedPatientUuid).bloodPressure.uuid
    val appointmentBpUuidOfLatestBpDeletedPatient = overdueAppointments.getValue(latestBpDeletedPatientUuid).bloodPressure.uuid
    val appointmentBpUuidOfOldestBpDeletedPatient = overdueAppointments.getValue(oldestBpNotDeletedPatientUuid).bloodPressure.uuid

    assertThat(appointmentBpUuidOfNoBpsDeletedPatient).isEqualTo(bpsForPatientWithNoBpsDeleted[1].uuid)
    assertThat(appointmentBpUuidOfLatestBpDeletedPatient).isEqualTo(bpsForPatientWithLatestBpDeleted[1].uuid)
    assertThat(appointmentBpUuidOfOldestBpDeletedPatient).isEqualTo(bpsForPatientWithOldestBpNotDeleted[0].uuid)
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
        currentFacility = facility
    ).blockingGet()
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofHours(24))

    val reminderDate = LocalDate.parse("2018-02-01")

    // when
    appointmentRepository.createReminder(appointmentUuid, reminderDate).blockingGet()

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
  fun when_marking_appointment_as_agreed_to_visit_reminder_for_30_days_should_be_set() {
    // given
    val appointmentScheduleDate = LocalDate.parse("2018-01-01")
    val appointmentScheduledAtTimestamp = Instant.now(clock)
    appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = appointmentUuid,
        appointmentDate = appointmentScheduleDate,
        appointmentType = Manual,
        currentFacility = facility
    ).blockingGet()
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofDays(1))

    // when
    appointmentRepository.markAsAgreedToVisit(appointmentUuid).blockingAwait()

    // then
    val appointmentUpdatedAtTimestamp = Instant.now(clock)
    with(getAppointmentByUuid(appointmentUuid)) {
      assertThat(remindOn).isEqualTo(LocalDate.parse("2018-02-01"))
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
        currentFacility = facility
    ).blockingGet()
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofDays(1))

    // when
    appointmentRepository.cancelWithReason(appointmentUuid, PatientNotResponding).blockingGet()

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
        currentFacility = facility
    ).blockingGet()
    markAppointmentSyncStatusAsDone(appointmentUuid)

    clock.advanceBy(Duration.ofDays(1))

    // when
    appointmentRepository.markAsAlreadyVisited(appointmentUuid).blockingAwait()

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
  fun high_risk_patients_should_be_present_at_the_top() {
    data class BP(val systolic: Int, val diastolic: Int)

    fun savePatientAndAppointment(
        patientUuid: UUID,
        appointmentUuid: UUID = UUID.randomUUID(),
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
          currentFacility = facility
      ).blockingGet()

      val bloodPressureMeasurements = bps.mapIndexed { index, (systolic, diastolic) ->

        val bpTimestamp = Instant.now(clock).plusSeconds(index.toLong() + 1)

        testData.bloodPressureMeasurement(
            patientUuid = patientUuid,
            systolic = systolic,
            diastolic = diastolic,
            userUuid = testData.qaUserUuid(),
            facilityUuid = facility.uuid,
            recordedAt = bpTimestamp,
            createdAt = bpTimestamp,
            updatedAt = bpTimestamp
        )
      }
      bpRepository.save(bloodPressureMeasurements).blockingAwait()

      medicalHistoryRepository.save(patientUuid, OngoingMedicalHistoryEntry(
          hasHadStroke = hasHadStroke,
          hasDiabetes = hasDiabetes,
          hasHadKidneyDisease = hasHadKidneyDisease,
          hasHadHeartAttack = hasHadHeartAttack
      )).blockingAwait()
      clock.advanceBy(Duration.ofSeconds(bps.size.toLong() + 1))
    }

    // given
    val thirtyDays = Duration.ofDays(30)
    val threeSixtyFiveDays = Duration.ofDays(366)

    savePatientAndAppointment(
        patientUuid = UUID.fromString("0620c310-0248-4d05-b7c4-8134bd7335e8"),
        fullName = "Has had a heart attack, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadHeartAttack = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("a3536489-5807-4ceb-98a2-7e0f508f28af"),
        fullName = "Has had a stroke, overdue == 20 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = Yes,
        appointmentHasBeenOverdueFor = Duration.ofDays(10)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("2c24c3a5-c385-4e5e-8643-b48ab28107c8"),
        fullName = "Has had a kidney disease, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadKidneyDisease = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("8b497bb5-f809-434c-b1a4-4efdf810f044"),
        fullName = "Has diabetes, overdue == 27 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasDiabetes = Yes,
        appointmentHasBeenOverdueFor = Duration.ofDays(27)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("cd78a254-a028-4b5d-bdcd-5ff367ad4143"),
        fullName = "Has had a heart attack, stroke, kidney disease and has diabetes, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = Yes,
        hasHadHeartAttack = Yes,
        hasHadKidneyDisease = Yes,
        hasDiabetes = Yes,
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("241d61a7-b4fd-43c3-9f7c-24ce31c7b0b7"),
        fullName = "Has had a heart attack, stroke, kidney disease and has diabetes, overdue > 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = Yes,
        hasHadHeartAttack = Yes,
        hasHadKidneyDisease = Yes,
        hasDiabetes = Yes,
        appointmentHasBeenOverdueFor = threeSixtyFiveDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("a8822899-afb9-4b20-af0d-dbee682c068d"),
        fullName = "Systolic > 180, overdue == 30 days",
        bps = listOf(BP(systolic = 9000, diastolic = 100)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("f50082f5-c7be-430e-998e-9d052735da36"),
        fullName = "Systolic > 180, overdue > 30 days",
        bps = listOf(BP(systolic = 9000, diastolic = 100)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("a2f9d14a-ece3-471e-a45b-c40263d3517f"),
        fullName = "Diastolic > 110, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("00dbcd51-a434-4a25-a1cf-1da66600082d"),
        fullName = "Diastolic > 110, overdue > 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("9f51709f-b356-4d9c-b6b7-1466eca35b78"),
        fullName = "Systolic > 180, overdue == 4 days",
        bps = listOf(BP(systolic = 9000, diastolic = 100)),
        appointmentHasBeenOverdueFor = Duration.ofDays(4)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("a4ed131e-c02e-469d-87d1-8aa63a9da780"),
        fullName = "Diastolic > 110, overdue == 3 days",
        bps = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = Duration.ofDays(3)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("b2a3fbd1-27eb-4ef4-b78d-f66cdbb164b4"),
        fullName = "Systolic == 179, overdue == 30 days",
        bps = listOf(BP(systolic = 179, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("ae1723d3-5162-4ae6-b483-016ed851ccbd"),
        fullName = "Systolic == 160, overdue == 30 days",
        bps = listOf(BP(systolic = 160, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("a6ca12c1-6f00-4ea9-82f7-b949be415471"),
        fullName = "Diastolic == 109, overdue == 30 days",
        bps = listOf(BP(systolic = 101, diastolic = 109)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("ab69af7f-70e8-44b2-8de3-fe591cd2741f"),
        fullName = "Diastolic == 100, overdue == 30 days",
        bps = listOf(BP(systolic = 101, diastolic = 100)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("e365514d-3301-43bc-b801-13ea49c0330d"),
        fullName = "Systolic == 159, overdue == 30 days",
        bps = listOf(BP(systolic = 159, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("5f872222-5389-453d-bc47-b48decaa091b"),
        fullName = "Systolic == 140, overdue == 30 days",
        bps = listOf(BP(systolic = 140, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("b9d7de3c-4f8e-4f99-b306-ca6df4bff2d1"),
        fullName = "Diastolic == 99, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 99)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("e947caf3-7dc2-46de-b269-36e7c561104a"),
        fullName = "Diastolic == 90, overdue == 30 days",
        bps = listOf(BP(systolic = 100, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("7742babb-10ca-4dce-9eae-59895251284d"),
        fullName = "BP == 139/89, overdue == 366 days",
        bps = listOf(BP(systolic = 139, diastolic = 89)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("234cbcb8-c3b1-4b7c-be34-7ac3691c1df7"),
        fullName = "BP == 141/91, overdue == 366 days",
        bps = listOf(BP(systolic = 141, diastolic = 91)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("6fe996ee-5792-4114-93dd-b79577600369"),
        fullName = "BP == 110/80, overdue == 366 days",
        bps = listOf(BP(systolic = 110, diastolic = 80)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("96c30123-9b08-4e11-b058-e80a62030a31"),
        fullName = "BP == 110/80, overdue between 30 days and 1 year",
        bps = listOf(BP(systolic = 110, diastolic = 80)),
        appointmentHasBeenOverdueFor = Duration.ofDays(80)
    )

    savePatientAndAppointment(
        patientUuid = UUID.fromString("a0388e84-3741-4bee-96d3-9a2335f0660b"),
        fullName = "Overdue == 3 days",
        bps = listOf(BP(systolic = 9000, diastolic = 9000)),
        appointmentHasBeenOverdueFor = Duration.ofDays(3)
    )

    // when
    val appointments = appointmentRepository.overdueAppointments(facility).blockingFirst()

    // then
    assertThat(appointments.map { it.fullName to it.riskLevel }).isEqualTo(listOf(
        "Systolic > 180, overdue > 30 days" to HIGHEST,
        "Diastolic > 110, overdue > 30 days" to HIGHEST,
        "Systolic > 180, overdue == 30 days" to HIGHEST,
        "Diastolic > 110, overdue == 30 days" to HIGHEST,
        "Has had a heart attack, stroke, kidney disease and has diabetes, overdue > 30 days" to VERY_HIGH,
        "Has had a heart attack, overdue == 30 days" to VERY_HIGH,
        "Has had a kidney disease, overdue == 30 days" to VERY_HIGH,
        "Has had a heart attack, stroke, kidney disease and has diabetes, overdue == 30 days" to VERY_HIGH,
        "Systolic == 179, overdue == 30 days" to HIGH,
        "Systolic == 160, overdue == 30 days" to HIGH,
        "Diastolic == 109, overdue == 30 days" to HIGH,
        "Diastolic == 100, overdue == 30 days" to HIGH,
        "BP == 141/91, overdue == 366 days" to REGULAR,
        "Systolic == 159, overdue == 30 days" to REGULAR,
        "Systolic == 140, overdue == 30 days" to REGULAR,
        "Diastolic == 99, overdue == 30 days" to REGULAR,
        "Diastolic == 90, overdue == 30 days" to REGULAR,
        "BP == 139/89, overdue == 366 days" to LOW,
        "BP == 110/80, overdue == 366 days" to LOW,
        "BP == 110/80, overdue between 30 days and 1 year" to NONE,
        "Has diabetes, overdue == 27 days" to NONE,
        "Has had a stroke, overdue == 20 days" to NONE,
        "Systolic > 180, overdue == 4 days" to NONE,
        "Diastolic > 110, overdue == 3 days" to NONE,
        "Overdue == 3 days" to NONE
    ))
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
        currentFacility = facility
    ).blockingGet()

    clock.advanceBy(Duration.ofDays(1))

    val scheduledDateForSecondAppointment = LocalDate.parse("2018-02-08")
    val secondAppointment = appointmentRepository.schedule(
        patientUuid = patientUuid,
        appointmentUuid = UUID.fromString("634b4807-d3a8-42a9-8411-7c921ed57f49"),
        appointmentDate = scheduledDateForSecondAppointment,
        appointmentType = Manual,
        currentFacility = facility
    ).blockingGet()

    // when
    val appointment = appointmentRepository.lastCreatedAppointmentForPatient(patientUuid).blockingFirst().toNullable()!!

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
        currentFacility = facility
    ).blockingGet()

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
      return appointmentRepository.schedule(
          patientUuid = patientProfile.patient.uuid,
          appointmentUuid = appointmentUuid,
          appointmentDate = LocalDate.parse("2017-12-30"),
          appointmentType = Manual,
          currentFacility = facility
      )
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
        .overdueAppointments(facility)
        .blockingFirst()
        .associateBy({ it.appointment.uuid }, { it.bloodPressure })

    // then
    val expected = mapOf(
        appointmentUuidForFirstPatient to laterRecordedBpForFirstPatient,
        appointmentUuidForSecondPatient to laterRecordedBpForSecondPatient
    )
    assertThat(bloodPressuresByAppointmentUuid).isEqualTo(expected)
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
    val overdueAppointments = appointmentRepository.overdueAppointments(facility).blockingFirst()

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
    val overdueAppointments = appointmentRepository.overdueAppointments(facility).blockingFirst()

    //then
    assertThat(overdueAppointments).hasSize(1)
    assertThat(overdueAppointments.first().appointment.patientUuid).isEqualTo(patientIdWithoutDeletedAppointment)
  }

  private fun markAppointmentSyncStatusAsDone(vararg appointmentUuids: UUID) {
    appointmentRepository.setSyncStatus(appointmentUuids.toList(), DONE).blockingAwait()
  }

  private fun getAppointmentByUuid(appointmentUuid: UUID): Appointment {
    return database.appointmentDao().getOne(appointmentUuid)!!
  }
}
