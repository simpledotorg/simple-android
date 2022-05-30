package org.simple.clinic.benchmark.patientregistration

import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.sharedTestCode.TestData
import org.simple.clinic.benchmark.BaseBenchmarkTest
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.uuid.UuidGenerator
import javax.inject.Inject

class PatientRegistrationBenchmark : BaseBenchmarkTest() {

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var medicalHistoryRepository: MedicalHistoryRepository

  @Inject
  lateinit var uuidGenerator: UuidGenerator

  init {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_patient_profile_and_medical_history() {
    val patientUuid = uuidGenerator.v4()
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        patientAddressUuid = uuidGenerator.v4(),
        syncStatus = SyncStatus.DONE,
        generatePhoneNumber = true,
        generateBusinessId = true,
        patientStatus = PatientStatus.Active,
        patientDeletedAt = null,
        generateDateOfBirth = true
    )

    val medicalHistory = TestData.medicalHistory(
        uuid = uuidGenerator.v4(),
        patientUuid = patientUuid,
        syncStatus = SyncStatus.DONE
    )

    patientRepository.save(listOf(patientProfile))
    medicalHistoryRepository.save(listOf(medicalHistory))
  }
}
