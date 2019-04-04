package org.simple.clinic.overdue

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.HIGH
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.HIGHEST
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.LOW
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.NONE
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.REGULAR
import org.simple.clinic.home.overdue.OverdueAppointment.RiskLevel.VERY_HIGH
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.NO
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.YES
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.Status.CANCELLED
import org.simple.clinic.overdue.Appointment.Status.SCHEDULED
import org.simple.clinic.overdue.Appointment.Status.VISITED
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UtcClock
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
  lateinit var clock: UtcClock

  private val testClock: TestUtcClock
    get() = clock as TestUtcClock

  private val authenticationRule = AuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    testClock.setYear(2018)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    testClock.resetToEpoch()
  }

  @Test
  fun when_creating_new_appointment_then_the_appointment_should_be_saved() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingGet()

    val savedAppointment = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet().first()
    savedAppointment.apply {
      assertThat(this.patientUuid).isEqualTo(patientId)
      assertThat(this.scheduledDate).isEqualTo(appointmentDate)
      assertThat(this.status).isEqualTo(Appointment.Status.SCHEDULED)
      assertThat(this.cancelReason).isEqualTo(null)
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
    }
  }

  @Test
  fun when_creating_new_appointment_then_all_old_appointments_for_that_patient_should_be_canceled() {
    val patientId = UUID.randomUUID()

    val date1 = LocalDate.now(clock)
    val timeOfSchedule = Instant.now(clock)
    appointmentRepository.schedule(patientId, date1).blockingGet()

    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofHours(24))

    val date2 = LocalDate.now(clock).plusDays(10)
    appointmentRepository.schedule(patientId, date2).blockingGet()

    val savedAppointment = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(savedAppointment).hasSize(2)

    val oldAppointment = savedAppointment[0]
    oldAppointment.apply {
      assertThat(this.patientUuid).isEqualTo(patientId)
      assertThat(this.scheduledDate).isEqualTo(date1)
      assertThat(this.status).isEqualTo(Appointment.Status.VISITED)
      assertThat(this.cancelReason).isEqualTo(null)
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
      assertThat(this.updatedAt).isNotEqualTo(timeOfSchedule)
      assertThat(this.updatedAt).isEqualTo(Instant.now(clock))
    }

    val newAppointment = savedAppointment[1]
    newAppointment.apply {
      assertThat(this.patientUuid).isEqualTo(patientId)
      assertThat(this.scheduledDate).isEqualTo(date2)
      assertThat(this.status).isEqualTo(SCHEDULED)
      assertThat(this.cancelReason).isEqualTo(null)
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
    }
  }

  @Test
  fun when_fetching_appointments_then_only_return_overdue_appointments() {
    val address1 = UUID.randomUUID()
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
    val patient1 = UUID.randomUUID()
    val date1 = LocalDate.now(clock).minusDays(100)
    val bp1 = UUID.randomUUID()
    database.patientDao().save(
        Patient(
            patient1,
            address1,
            faker.name.name(),
            faker.name.name(),
            Gender.FEMALE,
            LocalDate.parse("1947-08-15"),
            null,
            PatientStatus.ACTIVE,
            Instant.now(clock),
            Instant.now(clock),
            null,
            SyncStatus.DONE)
    )
    database.bloodPressureDao().save(listOf(
        BloodPressureMeasurement(
            uuid = bp1,
            systolic = 190,
            diastolic = 100,
            syncStatus = SyncStatus.PENDING,
            userUuid = testData.qaUserUuid(),
            facilityUuid = testData.qaUserFacilityUuid(),
            patientUuid = patient1,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null
        )
    ))

    val patient2 = UUID.randomUUID()
    val address2 = UUID.randomUUID()
    val phoneNumber2 = UUID.randomUUID()
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
            searchableName = faker.name.name(),
            gender = Gender.TRANSGENDER,
            dateOfBirth = LocalDate.parse("1997-08-15"),
            age = null,
            status = PatientStatus.ACTIVE,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null,
            syncStatus = SyncStatus.DONE
        )
    )
    database.phoneNumberDao().save(listOf(
        PatientPhoneNumber(
            uuid = phoneNumber2,
            patientUuid = patient2,
            number = "983374583",
            phoneType = PatientPhoneNumberType.MOBILE,
            active = false,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null
        ))
    )

    val patient3 = UUID.randomUUID()
    val address3 = UUID.randomUUID()
    val phoneNumber3 = UUID.randomUUID()
    val date3 = LocalDate.now(clock).minusDays(10)
    val bp30 = UUID.randomUUID()
    val bp31 = UUID.randomUUID()
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
            faker.name.name(),
            Gender.MALE,
            LocalDate.parse("1977-11-15"),
            null,
            PatientStatus.MIGRATED,
            Instant.now(clock),
            Instant.now(clock),
            null,
            SyncStatus.DONE)
    )
    database.phoneNumberDao().save(listOf(
        PatientPhoneNumber(
            uuid = phoneNumber3,
            patientUuid = patient3,
            number = "983374583",
            phoneType = PatientPhoneNumberType.MOBILE,
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
            syncStatus = SyncStatus.PENDING,
            userUuid = testData.qaUserUuid(),
            facilityUuid = testData.qaUserFacilityUuid(),
            patientUuid = patient3,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock),
            deletedAt = null
        ),
        BloodPressureMeasurement(
            uuid = bp31,
            systolic = 180,
            diastolic = 110,
            syncStatus = SyncStatus.PENDING,
            userUuid = testData.qaUserUuid(),
            facilityUuid = testData.qaUserFacilityUuid(),
            patientUuid = patient3,
            createdAt = Instant.now(clock).minusSeconds(1000),
            updatedAt = Instant.now(clock).minusSeconds(1000),
            deletedAt = null
        )
    ))

    appointmentRepository.schedule(patient1, date1).toCompletable()
        .andThen(appointmentRepository.schedule(patient2, LocalDate.now(clock).minusDays(2))).toCompletable()
        .andThen(appointmentRepository.schedule(patient3, date3))
        .blockingGet()

    val overdueAppts = appointmentRepository.overdueAppointments().blockingFirst()
    assertThat(overdueAppts).hasSize(1)

    overdueAppts[0].apply {
      assertThat(this.appointment.patientUuid).isEqualTo(patient3)
      assertThat(this.appointment.scheduledDate).isEqualTo(date3)
      assertThat(this.appointment.status).isEqualTo(SCHEDULED)
      assertThat(this.appointment.cancelReason).isEqualTo(null)
      assertThat(this.bloodPressure.uuid).isEqualTo(bp30)
    }
  }

  @Test
  fun deleted_blood_pressure_measurements_should_not_be_considered_when_fetching_overdue_appointments() {
    fun createBloodPressure(patientUuid: UUID, deletedAt: Instant? = null): BloodPressureMeasurement {
      return testData.bloodPressureMeasurement(
          patientUuid = patientUuid,
          facilityUuid = testData.qaUserFacilityUuid(),
          userUuid = testData.qaUserUuid(),
          syncStatus = SyncStatus.DONE,
          createdAt = Instant.now(),
          updatedAt = Instant.now(),
          deletedAt = deletedAt)
    }

    fun createAppointment(patientUuid: UUID, scheduledDate: LocalDate): Appointment {
      return testData.appointment(
          patientUuid = patientUuid,
          facilityUuid = testData.qaUserFacilityUuid(),
          status = SCHEDULED,
          scheduledDate = scheduledDate)
    }

    fun createPatient(fullName: String): PatientProfile {
      return testData.patientProfile(generatePhoneNumber = true)
          .let { patientProfile ->
            patientProfile.copy(patient = patientProfile.patient.copy(fullName = fullName))
          }
    }

    val patients = listOf(
        createPatient(fullName = "No BPs are deleted"),
        createPatient(fullName = "Latest BP is deleted"),
        createPatient(fullName = "Oldest BP is not deleted"),
        createPatient(fullName = "All BPs are deleted"))

    patientRepository.save(patients).blockingAwait()

    val bpsForPatient0 = patients[0].patient.let { patient ->
      listOf(
          createBloodPressure(patientUuid = patient.uuid),
          createBloodPressure(patientUuid = patient.uuid))
    }

    val bpsForPatient1 = patients[1].patient.let { patient ->
      listOf(
          createBloodPressure(patientUuid = patient.uuid),
          createBloodPressure(patientUuid = patient.uuid),
          createBloodPressure(patientUuid = patient.uuid, deletedAt = Instant.now(clock)))
    }

    val bpsForPatient2 = patients[2].patient.let { patient ->
      listOf(
          createBloodPressure(patientUuid = patient.uuid),
          createBloodPressure(patientUuid = patient.uuid, deletedAt = Instant.now(clock)),
          createBloodPressure(patientUuid = patient.uuid, deletedAt = Instant.now(clock)))
    }

    val bpsForPatient3 = patients[3].patient.let { patient ->
      listOf(
          createBloodPressure(patientUuid = patient.uuid, deletedAt = Instant.now(clock)),
          createBloodPressure(patientUuid = patient.uuid, deletedAt = Instant.now(clock)),
          createBloodPressure(patientUuid = patient.uuid, deletedAt = Instant.now(clock)))
    }

    bpRepository
        .save(bpsForPatient0 + bpsForPatient1 + bpsForPatient2 + bpsForPatient3)
        .blockingAwait()

    val today = LocalDate.now(clock)
    val appointmentsScheduledFor = today.minusDays(1L)

    val appointmentForPatient0 = createAppointment(
        patientUuid = patients[0].patient.uuid,
        scheduledDate = appointmentsScheduledFor)

    val appointmentForPatient1 = createAppointment(
        patientUuid = patients[1].patient.uuid,
        scheduledDate = appointmentsScheduledFor)

    val appointmentsForPatient2 = createAppointment(
        patientUuid = patients[2].patient.uuid,
        scheduledDate = appointmentsScheduledFor)

    val appointmentsForPatient3 = createAppointment(
        patientUuid = patients[3].patient.uuid,
        scheduledDate = appointmentsScheduledFor)

    appointmentRepository
        .save(listOf(appointmentForPatient0, appointmentForPatient1, appointmentsForPatient2, appointmentsForPatient3))
        .blockingAwait()

    val overdueAppointments = appointmentRepository.overdueAppointments().blockingFirst()
        .associateBy { it.fullName }

    assertThat(overdueAppointments.keys)
        .isEqualTo(setOf("No BPs are deleted", "Latest BP is deleted", "Oldest BP is not deleted"))

    assertThat(overdueAppointments["No BPs are deleted"]!!.bloodPressure.uuid).isEqualTo(bpsForPatient0[1].uuid)
    assertThat(overdueAppointments["Latest BP is deleted"]!!.bloodPressure.uuid).isEqualTo(bpsForPatient1[1].uuid)
    assertThat(overdueAppointments["Oldest BP is not deleted"]!!.bloodPressure.uuid).isEqualTo(bpsForPatient2[0].uuid)
  }

  @Test
  fun when_setting_appointment_reminder_then_reminder_with_correct_date_should_be_set() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    val timeOfSchedule = Instant.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingGet()

    val appointments = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().remindOn).isNull()

    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofHours(24))

    val uuid = appointments[0].uuid
    val reminderDate = LocalDate.now(clock).plusDays(10)
    appointmentRepository.createReminder(uuid, reminderDate).blockingGet()

    val updatedAppointments = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedAppointments).hasSize(1)
    updatedAppointments[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.remindOn).isEqualTo(reminderDate)
      assertThat(this.agreedToVisit).isNull()
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
      assertThat(this.updatedAt).isNotEqualTo(timeOfSchedule)
      assertThat(this.updatedAt).isEqualTo(Instant.now(clock))
    }
  }

  @Test
  fun when_marking_appointment_as_agreed_to_visit_reminder_for_30_days_should_be_set() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    val timeOfSchedule = Instant.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingGet()

    val appointments = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().remindOn).isNull()
    assertThat(appointments.first().agreedToVisit).isNull()

    val uuid = appointments[0].uuid
    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofDays(1))
    appointmentRepository.markAsAgreedToVisit(uuid).blockingAwait()

    val updatedList = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.remindOn).isEqualTo(LocalDate.now(clock).plusDays(30))
      assertThat(this.agreedToVisit).isTrue()
      assertThat(this.updatedAt).isNotEqualTo(timeOfSchedule)
      assertThat(this.updatedAt).isEqualTo(Instant.now(clock))
    }
  }

  @Test
  fun when_removing_appointment_from_list_then_appointment_status_and_cancel_reason_should_be_updated() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    val timeOfSchedule = Instant.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingGet()

    val appointments = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().cancelReason).isNull()

    val uuid = appointments[0].uuid
    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofDays(1))

    appointmentRepository.cancelWithReason(uuid, AppointmentCancelReason.PatientNotResponding).blockingGet()

    val updatedList = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.cancelReason).isEqualTo(AppointmentCancelReason.PatientNotResponding)
      assertThat(this.status).isEqualTo(CANCELLED)
      assertThat(this.updatedAt).isNotEqualTo(timeOfSchedule)
      assertThat(this.updatedAt).isEqualTo(Instant.now(clock))
    }
  }

  @Test
  fun when_removing_appointment_with_reason_as_patient_already_visited_then_appointment_should_be_marked_as_visited() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    val timeOfSchedule = Instant.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingGet()

    val appointments = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().status).isEqualTo(SCHEDULED)

    val uuid = appointments[0].uuid
    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofDays(1))

    appointmentRepository.markAsAlreadyVisited(uuid).blockingAwait()

    val updatedList = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.cancelReason).isNull()
      assertThat(this.status).isEqualTo(VISITED)
      assertThat(this.updatedAt).isEqualTo(Instant.now(clock))
      assertThat(this.updatedAt).isNotEqualTo(timeOfSchedule)
    }
  }

  @Test
  fun high_risk_patients_should_be_present_at_the_top() {
    data class BP(val systolic: Int, val diastolic: Int)

    fun savePatientAndAppointment(
        fullName: String,
        bpMeasurements: List<BP>,
        hasHadHeartAttack: MedicalHistory.Answer = NO,
        hasHadStroke: MedicalHistory.Answer = NO,
        hasDiabetes: MedicalHistory.Answer = NO,
        hasHadKidneyDisease: MedicalHistory.Answer = NO,
        appointmentHasBeenOverdueFor: Duration
    ) {
      val patientUuid = patientRepository.saveOngoingEntry(testData.ongoingPatientEntry(fullName = fullName, age = "30"))
          .andThen(patientRepository.saveOngoingEntryAsPatient())
          .blockingGet()
          .uuid

      val scheduledDate = (LocalDateTime.now(clock) - appointmentHasBeenOverdueFor).toLocalDate()
      appointmentRepository.schedule(patientUuid, scheduledDate).blockingGet()
      bpMeasurements.forEach {
        bpRepository.saveMeasurement(patientUuid, it.systolic, it.diastolic).blockingGet()
        testClock.advanceBy(Duration.ofSeconds(1))
      }
      medicalHistoryRepository.save(patientUuid, OngoingMedicalHistoryEntry(
          hasHadStroke = hasHadStroke,
          hasDiabetes = hasDiabetes,
          hasHadKidneyDisease = hasHadKidneyDisease,
          hasHadHeartAttack = hasHadHeartAttack
      )).blockingAwait()
      testClock.advanceBy(Duration.ofSeconds(1))
    }

    val thirtyDays = Duration.ofDays(30)
    val threeSixtyFiveDays = Duration.ofDays(366)

    savePatientAndAppointment(
        fullName = "Has had a heart attack, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadHeartAttack = YES,
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Has had a stroke, overdue == 20 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = YES,
        appointmentHasBeenOverdueFor = Duration.ofDays(10))

    savePatientAndAppointment(
        fullName = "Has had a kidney disease, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadKidneyDisease = YES,
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Has diabetes, overdue == 27 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 90)),
        hasDiabetes = YES,
        appointmentHasBeenOverdueFor = Duration.ofDays(27))

    savePatientAndAppointment(
        fullName = "Has had a heart attack, stroke, kidney disease and has diabetes, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = YES,
        hasHadHeartAttack = YES,
        hasHadKidneyDisease = YES,
        hasDiabetes = YES,
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Has had a heart attack, stroke, kidney disease and has diabetes, overdue > 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = YES,
        hasHadHeartAttack = YES,
        hasHadKidneyDisease = YES,
        hasDiabetes = YES,
        appointmentHasBeenOverdueFor = threeSixtyFiveDays)

    savePatientAndAppointment(
        fullName = "Systolic > 180, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 9000, diastolic = 100)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Systolic > 180, overdue > 30 days",
        bpMeasurements = listOf(BP(systolic = 9000, diastolic = 100)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays)

    savePatientAndAppointment(
        fullName = "Diastolic > 110, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Diastolic > 110, overdue > 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays)

    savePatientAndAppointment(
        fullName = "Systolic > 180, overdue == 4 days",
        bpMeasurements = listOf(BP(systolic = 9000, diastolic = 100)),
        appointmentHasBeenOverdueFor = Duration.ofDays(4))

    savePatientAndAppointment(
        fullName = "Diastolic > 110, overdue == 3 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 9000)),
        appointmentHasBeenOverdueFor = Duration.ofDays(3))

    savePatientAndAppointment(
        fullName = "Systolic == 179, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 179, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Systolic == 160, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 160, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Diastolic == 109, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 101, diastolic = 109)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Diastolic == 100, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 101, diastolic = 100)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Systolic == 159, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 159, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Systolic == 140, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 140, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Diastolic == 99, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 99)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "Diastolic == 90, overdue == 30 days",
        bpMeasurements = listOf(BP(systolic = 100, diastolic = 90)),
        appointmentHasBeenOverdueFor = thirtyDays)

    savePatientAndAppointment(
        fullName = "BP == 139/89, overdue == 366 days",
        bpMeasurements = listOf(BP(systolic = 139, diastolic = 89)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays)

    savePatientAndAppointment(
        fullName = "BP == 141/91, overdue == 366 days",
        bpMeasurements = listOf(BP(systolic = 141, diastolic = 91)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays)

    savePatientAndAppointment(
        fullName = "BP == 110/80, overdue == 366 days",
        bpMeasurements = listOf(BP(systolic = 110, diastolic = 80)),
        appointmentHasBeenOverdueFor = threeSixtyFiveDays)

    savePatientAndAppointment(
        fullName = "BP == 110/80, overdue between 30 days and 1 year",
        bpMeasurements = listOf(BP(systolic = 110, diastolic = 80)),
        appointmentHasBeenOverdueFor = Duration.ofDays(80))

    savePatientAndAppointment(
        fullName = "Overdue == 3 days",
        bpMeasurements = listOf(BP(systolic = 9000, diastolic = 9000)),
        appointmentHasBeenOverdueFor = Duration.ofDays(3))

    val appointments = appointmentRepository.overdueAppointments().blockingFirst()

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
    val patientId = UUID.randomUUID()
    val scheduledDateNextMonth = LocalDate.now(testClock).plusMonths(1)
    appointmentRepository.schedule(patientId, scheduledDateNextMonth).blockingGet()

    val scheduledDateNextWeek = LocalDate.now(testClock).plusWeeks(1)
    testClock.advanceBy(Duration.ofDays(1))
    val secondAppointment = appointmentRepository.schedule(patientId, scheduledDateNextWeek).blockingGet()

    val (appointment) = appointmentRepository.lastCreatedAppointmentForPatient(patientId).blockingFirst()
    assertThat(appointment!!).isEqualTo(secondAppointment)
  }

  @Test
  fun marking_appointment_older_than_current_date_should_work_correctly() {
    val patientId = UUID.randomUUID()
    val appointmentUuid1 = UUID.randomUUID()
    val appointmentUuid2 = UUID.randomUUID()
    val scheduleDate = LocalDate.now(testClock).plusMonths(1)

    testClock.advanceBy(Duration.ofHours(1))
    database
        .appointmentDao()
        .save(listOf(testData.appointment(
            uuid = appointmentUuid1,
            patientUuid = patientId,
            status = SCHEDULED,
            syncStatus = SyncStatus.DONE,
            scheduledDate = scheduleDate,
            createdAt = Instant.now(testClock),
            updatedAt = Instant.now(testClock)
        )))
    val firstAppointment = database.appointmentDao().getOne(appointmentUuid1)
    testClock.advanceBy(Duration.ofHours(1))

    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientId).blockingAwait()

    assertThat(database.appointmentDao().getOne(appointmentUuid1)).isEqualTo(firstAppointment)

    testClock.advanceBy(Duration.ofDays(1))

    database
        .appointmentDao()
        .save(listOf(testData.appointment(
            uuid = appointmentUuid2,
            patientUuid = patientId,
            scheduledDate = scheduleDate,
            status = SCHEDULED,
            syncStatus = SyncStatus.PENDING,
            createdAt = Instant.now(testClock),
            updatedAt = Instant.now(testClock)
        )))

    val secondAppointment = database.appointmentDao().getOne(appointmentUuid2)
    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientId).blockingAwait()

    database.appointmentDao().getOne(firstAppointment!!.uuid)!!.run {
      assertThat(status).isEqualTo(VISITED)
      assertThat(syncStatus).isEqualTo(SyncStatus.PENDING)
      assertThat(updatedAt).isEqualTo(Instant.now(testClock))
    }
    assertThat(database.appointmentDao().getOne(appointmentUuid2)).isEqualTo(secondAppointment)
  }

  @Test
  fun when_scheduling_appointment_for_defaulter_patient_then_the_appointment_should_be_saved_as_defaulter() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    appointmentRepository.schedule(
        patientUuid = patientId,
        appointmentDate = appointmentDate,
        appointmentType = Automatic
    ).blockingGet()

    val savedAppointment = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet().first()
    savedAppointment.let {
      assertThat(it.patientUuid).isEqualTo(patientId)
      assertThat(it.scheduledDate).isEqualTo(appointmentDate)
      assertThat(it.status).isEqualTo(Appointment.Status.SCHEDULED)
      assertThat(it.syncStatus).isEqualTo(SyncStatus.PENDING)
      assertThat(it.appointmentType).isEqualTo(Automatic)
    }
  }
}
