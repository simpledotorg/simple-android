package org.simple.clinic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.Lazy
import io.bloco.faker.Faker
import io.reactivex.Completable
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.ReminderConsent
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

private const val EXTRA_PATIENT_COUNT = "patient_count"

/**
 * Broadcast Receiver to generate fake data.
 *
 * # Usage
 * ```
 * adb shell am broadcast -a org.simple.clinic.debug.GENERATE_FAKE_DATA -p org.simple.clinic.qa.debug --ei patient_count 2000
 * ```
 **/
class FakeDataGenerationReceiver : BroadcastReceiver() {

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var bloodPressureRepository: BloodPressureRepository

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  @Inject
  lateinit var user: Lazy<User>

  @Inject
  lateinit var clock: UtcClock

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var uuidGenerator: UuidGenerator

  private val faker = Faker("en-IND")

  private val genders = setOf(Gender.Female, Gender.Male, Gender.Transgender)

  override fun onReceive(context: Context, intent: Intent) {
    DebugClinicApp.appComponent().inject(this)

    generateFakeData(intent.getIntExtra(EXTRA_PATIENT_COUNT, 0))
        .subscribeOn(schedulersProvider.io())
        .subscribe()
  }

  private fun generateFakeData(recordsToGenerate: Int): Completable {
    return Completable.fromAction {

      val currentUser = user.get()
      val currentFacility = facilityRepository.currentFacilityImmediate()!!
      val otherFacility = anyFacilityExceptCurrent(currentUser, currentFacility)

      val records = generateRecords(recordsToGenerate, currentFacility, otherFacility, currentUser)

      val patients = records.map { it.patientProfile }
      val bps = records.map { it.bloodPressureMeasurements }.flatten()
      val appointments = records.map { it.appointment }

      patientRepository.save(patients)
          .andThen(bloodPressureRepository.save(bps))
          .andThen(appointmentRepository.save(appointments))
          .blockingAwait()
    }
  }

  private fun anyFacilityExceptCurrent(
      user: User,
      currentFacility: Facility
  ): Facility {
    return facilityRepository
        .facilitiesInCurrentGroup(user = user)
        .map { allFacilities -> allFacilities.first { it.uuid != currentFacility.uuid } }
        .blockingFirst()
  }

  private fun generateRecords(
      recordsToGenerate: Int,
      currentFacility: Facility,
      otherFacility: Facility,
      user: User
  ): List<Record> {
    return (1..recordsToGenerate)
        .map {
          val facility = if (Random.nextFloat() > 0.5) currentFacility else otherFacility
          generatePatientRecord(user, facility)
        }
        .toList()
  }

  private fun generatePatientRecord(
      user: User,
      facility: Facility
  ): Record {
    val now = Instant.now(clock)

    val address = patientAddress(now)
    val patient = patient(address, now)
    val phoneNumber = patientPhoneNumber(patient, now)

    val patientProfile = PatientProfile(
        patient = patient,
        address = address,
        phoneNumbers = listOf(phoneNumber),
        businessIds = emptyList()
    )

    val bloodPressureMeasurement = (1..10).map { bloodPressureMeasurement(user, facility, patient, now) }
    val appointment = appointment(patient, facility)

    return Record(patientProfile, bloodPressureMeasurement, appointment)
  }

  private fun appointment(
      patient: Patient,
      facility: Facility
  ): Appointment {
    val today = LocalDate.now(clock)
    val now = Instant.now(clock)

    return Appointment(
        uuid = uuidGenerator.v4(),
        facilityUuid = facility.uuid,
        patientUuid = patient.uuid,
        scheduledDate = today.minusDays(Random.nextInt(2..30).toLong()),
        status = Appointment.Status.Scheduled,
        cancelReason = null,
        remindOn = null,
        agreedToVisit = false,
        appointmentType = Appointment.AppointmentType.Manual,
        syncStatus = SyncStatus.DONE,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        creationFacilityUuid = facility.uuid
    )
  }

  private fun bloodPressureMeasurement(
      user: User,
      facility: Facility,
      patient: Patient,
      timestamp: Instant
  ): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = uuidGenerator.v4(),
        reading = BloodPressureReading(Random.nextInt(90..200), Random.nextInt(60..140)),
        syncStatus = SyncStatus.DONE,
        userUuid = user.uuid,
        facilityUuid = facility.uuid,
        patientUuid = patient.uuid,
        createdAt = timestamp,
        updatedAt = timestamp,
        deletedAt = null,
        recordedAt = timestamp
    )
  }

  private fun patientPhoneNumber(
      patient: Patient,
      timestamp: Instant
  ): PatientPhoneNumber {
    return PatientPhoneNumber(
        uuid = uuidGenerator.v4(),
        patientUuid = patient.uuid,
        number = faker.phoneNumber.phoneNumber(),
        phoneType = PatientPhoneNumberType.Mobile,
        active = true,
        createdAt = timestamp,
        updatedAt = timestamp,
        deletedAt = null
    )
  }

  private fun patient(address: PatientAddress, timestamp: Instant): Patient {
    return Patient(
        uuid = uuidGenerator.v4(),
        addressUuid = address.uuid,
        fullName = faker.name.name(),
        gender = randomGender(),
        dateOfBirth = LocalDate.now(clock).minusYears(Random.nextInt(30..70).toLong()),
        age = null,
        status = PatientStatus.Active,
        createdAt = timestamp,
        updatedAt = timestamp,
        deletedAt = null,
        recordedAt = timestamp,
        syncStatus = SyncStatus.DONE,
        reminderConsent = ReminderConsent.Granted,
        deletedReason = null,
        registeredFacilityId = null,
        assignedFacilityId = null
    )
  }

  private fun patientAddress(timestamp: Instant): PatientAddress {
    return PatientAddress(
        uuid = uuidGenerator.v4(),
        streetAddress = faker.address.streetAddress(),
        colonyOrVillage = faker.address.streetAddress(),
        district = faker.address.city(),
        zone = faker.address.secondaryAddress(),
        state = faker.address.state(),
        country = faker.address.country(),
        createdAt = timestamp,
        updatedAt = timestamp,
        deletedAt = null
    )
  }

  private data class Record(
      val patientProfile: PatientProfile,
      val bloodPressureMeasurements: List<BloodPressureMeasurement>,
      val appointment: Appointment
  )

  private fun randomGender(): Gender = genders.random()
}
