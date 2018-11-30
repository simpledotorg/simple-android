package org.simple.clinic.overdue

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.TestClock
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
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
  lateinit var clock: Clock

  private val testClock: TestClock
    get() = clock as TestClock

  @Inject
  lateinit var config: Single<AppointmentConfig>

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    testClock.setYear(2018)
  }

  @Test
  fun when_creating_new_appointment_then_the_appointment_should_be_saved() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingAwait()

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
    appointmentRepository.schedule(patientId, date1).blockingAwait()

    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofHours(24))

    val date2 = LocalDate.now(clock).plusDays(10)
    appointmentRepository.schedule(patientId, date2).blockingAwait()

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
      assertThat(this.status).isEqualTo(Appointment.Status.SCHEDULED)
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
            Instant.now(clock)
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
            SyncStatus.DONE
        )
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
            updatedAt = Instant.now(clock)
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
            Instant.now(clock)
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
            updatedAt = Instant.now(clock)
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
            Instant.now(clock)
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
            SyncStatus.DONE
        )
    )
    database.phoneNumberDao().save(listOf(
        PatientPhoneNumber(
            uuid = phoneNumber3,
            patientUuid = patient3,
            number = "983374583",
            phoneType = PatientPhoneNumberType.MOBILE,
            active = true,
            createdAt = Instant.now(clock),
            updatedAt = Instant.now(clock)
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
            updatedAt = Instant.now(clock)
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
            updatedAt = Instant.now(clock).minusSeconds(1000)
        )
    ))

    appointmentRepository.schedule(patient1, date1)
        .andThen(appointmentRepository.schedule(patient2, LocalDate.now(clock).minusDays(2)))
        .andThen(appointmentRepository.schedule(patient3, date3))
        .blockingGet()

    val overdueAppts = appointmentRepository.overdueAppointments().blockingFirst()
    assertThat(overdueAppts).hasSize(1)

    overdueAppts[0].apply {
      assertThat(this.appointment.patientUuid).isEqualTo(patient3)
      assertThat(this.appointment.scheduledDate).isEqualTo(date3)
      assertThat(this.appointment.status).isEqualTo(Appointment.Status.SCHEDULED)
      assertThat(this.appointment.cancelReason).isEqualTo(null)
      assertThat(this.bloodPressure.uuid).isEqualTo(bp30)
    }
  }

  @Test
  fun when_setting_appointment_reminder_then_reminder_with_correct_date_should_be_set() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    val timeOfSchedule = Instant.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingAwait()

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
    appointmentRepository.schedule(patientId, appointmentDate).blockingAwait()

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
    appointmentRepository.schedule(patientId, appointmentDate).blockingAwait()

    val appointments = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().cancelReason).isNull()

    val uuid = appointments[0].uuid
    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofDays(1))

    appointmentRepository.cancelWithReason(uuid, Appointment.CancelReason.PATIENT_NOT_RESPONDING).blockingGet()

    val updatedList = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.cancelReason).isEqualTo(Appointment.CancelReason.PATIENT_NOT_RESPONDING)
      assertThat(this.status).isEqualTo(Appointment.Status.CANCELLED)
      assertThat(this.updatedAt).isNotEqualTo(timeOfSchedule)
      assertThat(this.updatedAt).isEqualTo(Instant.now(clock))
    }
  }

  @Test
  fun when_removing_appointment_with_reason_as_patient_already_visited_then_appointment_should_be_marked_as_visited() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    val timeOfSchedule = Instant.now(clock)
    appointmentRepository.schedule(patientId, appointmentDate).blockingAwait()

    val appointments = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().status).isEqualTo(Appointment.Status.SCHEDULED)

    val uuid = appointments[0].uuid
    appointmentRepository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE).blockingAwait()

    testClock.advanceBy(Duration.ofDays(1))

    appointmentRepository.markAsAlreadyVisited(uuid).blockingAwait()

    val updatedList = appointmentRepository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.cancelReason).isNull()
      assertThat(this.status).isEqualTo(Appointment.Status.VISITED)
      assertThat(this.updatedAt).isEqualTo(Instant.now(clock))
      assertThat(this.updatedAt).isNotEqualTo(timeOfSchedule)
    }
  }

  @Test
  fun high_risk_patients_should_be_present_at_the_top() {
    data class BP(val systolic: Int, val diastolic: Int)

    fun savePatientAndAppointment(
        fullName: String,
        hasHadStroke: Boolean = false,
        hasDiabetes: Boolean = false,
        hasHadKidneyDisease: Boolean = false,
        age: String = "30",
        vararg bpMeasurements: BP
    ) {
      val patientUuid = patientRepository.saveOngoingEntry(testData.ongoingPatientEntry(fullName = fullName, age = age))
          .andThen(patientRepository.saveOngoingEntryAsPatient())
          .blockingGet()
          .uuid
      appointmentRepository.schedule(patientUuid, LocalDate.now(clock).minusDays(2)).blockingAwait()
      bpMeasurements.forEach {
        bpRepository.saveMeasurement(patientUuid, it.systolic, it.diastolic).blockingGet()
        testClock.advanceBy(Duration.ofSeconds(1))
      }
      medicalHistoryRepository.save(patientUuid, OngoingMedicalHistoryEntry(
          hasHadStroke = hasHadStroke,
          hasDiabetes = hasDiabetes,
          hasHadKidneyDisease = hasHadKidneyDisease
      )).blockingAwait()
      testClock.advanceBy(Duration.ofSeconds(1))
    }

    savePatientAndAppointment(
        fullName = "Normal + older",
        bpMeasurements = *arrayOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = false)
    testClock.advanceBy(Duration.ofSeconds(1))

    savePatientAndAppointment(
        fullName = "Normal + recent",
        bpMeasurements = *arrayOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = false)
    testClock.advanceBy(Duration.ofSeconds(1))

    savePatientAndAppointment(
        fullName = "With stroke",
        bpMeasurements = *arrayOf(BP(systolic = 100, diastolic = 90)),
        hasHadStroke = true)
    testClock.advanceBy(Duration.ofSeconds(1))

    savePatientAndAppointment(
        fullName = "Age > 60, last BP > 160/100 and diabetes",
        age = "61",
        bpMeasurements = *arrayOf(BP(systolic = 170, diastolic = 120)),
        hasDiabetes = true)
    testClock.advanceBy(Duration.ofSeconds(1))

    savePatientAndAppointment(
        fullName = "Age == 60, last BP == 160/100 and kidney disease",
        age = "60",
        bpMeasurements = *arrayOf(BP(systolic = 160, diastolic = 100)),
        hasHadKidneyDisease = true,
        hasHadStroke = false)
    testClock.advanceBy(Duration.ofSeconds(1))

    savePatientAndAppointment(
        fullName = "Age > 60, second last BP > 160/100 and kidney disease",
        age = "61",
        bpMeasurements = *arrayOf(BP(systolic = 170, diastolic = 120), BP(100, 100)),
        hasHadKidneyDisease = true,
        hasHadStroke = false)
    testClock.advanceBy(Duration.ofSeconds(1))

    savePatientAndAppointment(
        fullName = "Age < 60, last BP > 160/100 and kidney disease",
        age = "31",
        bpMeasurements = *arrayOf(BP(systolic = 170, diastolic = 120)),
        hasHadKidneyDisease = true,
        hasHadStroke = false)
    testClock.advanceBy(Duration.ofSeconds(1))

    val appointments = appointmentRepository.overdueAppointments().blockingFirst()

    val config = config.blockingGet()
    if (config.highlightHighRiskPatients.not()) {
      assertThat(appointments.firstOrNull { it.isAtHighRisk }).isNull()

    } else {
      assertThat(appointments.map { it.fullName to it.isAtHighRisk }).isEqualTo(listOf(
          "With stroke" to true,
          "Age > 60, last BP > 160/100 and diabetes" to true,
          "Age == 60, last BP == 160/100 and kidney disease" to true,
          "Normal + older" to false,
          "Normal + recent" to false,
          "Age > 60, second last BP > 160/100 and kidney disease" to false,
          "Age < 60, last BP > 160/100 and kidney disease" to false
      ))
    }
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    testClock.resetToEpoch()
  }
}
