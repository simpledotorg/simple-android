package org.simple.clinic.overdue

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AppointmentRepositoryAndroidTest {

  @Inject
  lateinit var repository: AppointmentRepository

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

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_creating_new_appointment_then_the_appointment_should_be_saved() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now()
    repository.schedule(patientId, appointmentDate).blockingAwait()

    val savedAppointment = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet().first()
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

    val date1 = LocalDate.now()
    repository.schedule(patientId, date1).blockingAwait()

    val date2 = LocalDate.now().plusDays(10)
    repository.schedule(patientId, date2).blockingAwait()

    val savedAppointment = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(savedAppointment).hasSize(2)

    savedAppointment[0].apply {
      assertThat(this.patientUuid).isEqualTo(patientId)
      assertThat(this.scheduledDate).isEqualTo(date1)
      assertThat(this.status).isEqualTo(Appointment.Status.VISITED)
      assertThat(this.cancelReason).isEqualTo(null)
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
    }

    savedAppointment[1].apply {
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
            Instant.now(),
            Instant.now()
        )
    )
    val patient1 = UUID.randomUUID()
    val date1 = LocalDate.now().minusDays(100)
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
            Instant.now(),
            Instant.now(),
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
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    ))

    val patient2 = UUID.randomUUID()
    val address2 = UUID.randomUUID()
    database.addressDao().save(
        PatientAddress(
            address2,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            Instant.now(),
            Instant.now()
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
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            syncStatus = SyncStatus.DONE
        )
    )

    val patient3 = UUID.randomUUID()
    val address3 = UUID.randomUUID()
    val date3 = LocalDate.now().minusDays(10)
    val bp30 = UUID.randomUUID()
    val bp31 = UUID.randomUUID()
    database.addressDao().save(
        PatientAddress(
            address3,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            Instant.now(),
            Instant.now()
        )
    )
    database.patientDao().save(
        Patient(
            patient3,
            address3,
            faker.name.name(),
            faker.name.name(),
            Gender.MALE,
            LocalDate.parse("1977-11-15"),
            null,
            PatientStatus.MIGRATED,
            Instant.now(),
            Instant.now(),
            SyncStatus.DONE
        )
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
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        ),
        BloodPressureMeasurement(
            uuid = bp31,
            systolic = 180,
            diastolic = 110,
            syncStatus = SyncStatus.PENDING,
            userUuid = testData.qaUserUuid(),
            facilityUuid = testData.qaUserFacilityUuid(),
            patientUuid = patient3,
            createdAt = Instant.now().minusSeconds(1000),
            updatedAt = Instant.now().minusSeconds(1000)
        )
    ))

    repository.schedule(patient1, date1)
        .andThen(repository.schedule(patient2, LocalDate.now().minusDays(2)))
        .andThen(repository.schedule(patient3, date3))
        .blockingGet()

    val overdueAppts = repository.overdueAppointments().blockingFirst()
    assertThat(overdueAppts).hasSize(2)
    overdueAppts[0].apply {
      assertThat(this.appointment.patientUuid).isEqualTo(patient1)
      assertThat(this.appointment.scheduledDate).isEqualTo(date1)
      assertThat(this.appointment.status).isEqualTo(Appointment.Status.SCHEDULED)
      assertThat(this.appointment.cancelReason).isEqualTo(null)
      assertThat(this.bloodPressure.uuid).isEqualTo(bp1)
    }
    overdueAppts[1].apply {
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
    val appointmentDate = LocalDate.now()
    repository.schedule(patientId, appointmentDate).blockingAwait()

    val appointments = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().remindOn).isNull()

    val uuid = appointments[0].uuid
    repository.setSyncStatus(listOf(uuid), SyncStatus.DONE).blockingGet()

    val reminderDate = LocalDate.now().plusDays(10)
    repository.createReminder(uuid, reminderDate).blockingGet()

    val updatedList = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.remindOn).isEqualTo(reminderDate)
      assertThat(this.agreedToVisit).isNull()
    }
  }

  @Test
  fun when_marking_appointment_as_agreed_to_visit_reminder_for_30_days_should_be_set() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now(clock)
    repository.schedule(patientId, appointmentDate).blockingAwait()

    val appointments = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().remindOn).isNull()
    assertThat(appointments.first().agreedToVisit).isNull()

    val uuid = appointments[0].uuid
    repository.setSyncStatus(listOf(uuid), SyncStatus.DONE).blockingGet()
    repository.markAsAgreedToVisit(uuid).blockingGet()

    val updatedList = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.remindOn).isEqualTo(LocalDate.now(clock).plusDays(30))
      assertThat(this.agreedToVisit).isTrue()
    }
  }

  @Test
  fun when_removing_appointment_from_list_then_appointment_status_and_cancel_reason_should_be_updated() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now()
    repository.schedule(patientId, appointmentDate).blockingAwait()

    val appointments = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().cancelReason).isNull()

    val uuid = appointments[0].uuid
    repository.setSyncStatus(listOf(uuid), SyncStatus.DONE).blockingGet()
    repository.cancelWithReason(uuid, Appointment.CancelReason.PATIENT_NOT_RESPONDING).blockingGet()

    val updatedList = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.cancelReason).isEqualTo(Appointment.CancelReason.PATIENT_NOT_RESPONDING)
      assertThat(this.status).isEqualTo(Appointment.Status.CANCELLED)
    }
  }

  @Test
  fun when_removing_appointment_with_reason_as_patient_already_visited_then_appointment_should_be_marked_as_visited() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now()
    repository.schedule(patientId, appointmentDate).blockingAwait()

    val appointments = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(appointments).hasSize(1)
    assertThat(appointments.first().status).isEqualTo(Appointment.Status.SCHEDULED)

    val uuid = appointments[0].uuid
    repository.setSyncStatus(listOf(uuid), SyncStatus.DONE).blockingGet()
    repository.markAsVisited(uuid).blockingGet()

    val updatedList = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()
    assertThat(updatedList).hasSize(1)
    updatedList[0].apply {
      assertThat(this.uuid).isEqualTo(uuid)
      assertThat(this.cancelReason).isNull()
      assertThat(this.status).isEqualTo(Appointment.Status.VISITED)
    }
  }
}
