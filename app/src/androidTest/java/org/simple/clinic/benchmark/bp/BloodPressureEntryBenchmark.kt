package org.simple.clinic.benchmark.bp

import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.benchmark.BaseBenchmarkTest
import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
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

  init {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun `creating_new_blood_pressure_entry_for_a_patient`() {
    val patientUuid = uuidGenerator.v4()
    val recordedAt = Instant.now()

    bloodPressureRepository.saveMeasurement(
        patientUuid = patientUuid,
        reading = BloodPressureReading(systolic = 120, diastolic = 80),
        loggedInUser = TestData.loggedInUser(uuidGenerator.v4(), name = "Anand Rai"),
        currentFacility = TestData.facility(uuidGenerator.v4()),
        recordedAt = recordedAt,
        uuid = uuidGenerator.v4()
    )

    appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)

    patientRepository.compareAndUpdateRecordedAt(patientUuid, recordedAt)
  }
}
