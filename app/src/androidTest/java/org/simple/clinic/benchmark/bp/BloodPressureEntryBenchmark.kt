package org.simple.clinic.benchmark.bp

import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.benchmark.BaseBenchmarkTest
import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class BloodPressureEntryBenchmark : BaseBenchmarkTest() {

  @Inject
  lateinit var bloodPressureRepository: BloodPressureRepository

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var uuidGenerator: UuidGenerator

  @Inject
  lateinit var facility: Facility

  @Inject
  lateinit var user: User

  init {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun creating_new_blood_pressure_entry_for_a_patient() {
    val patientUuid = UUID.fromString("9a59d117-34ad-44e5-b0e2-93213ebfea01")
    val recordedAt = Instant.now()

    bloodPressureRepository.saveMeasurement(
        patientUuid = patientUuid,
        reading = BloodPressureReading(systolic = 120, diastolic = 80),
        loggedInUser = user,
        currentFacility = facility,
        recordedAt = recordedAt,
        uuid = uuidGenerator.v4()
    )

    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)

    patientRepository.compareAndUpdateRecordedAt(patientUuid, recordedAt)
  }
}
