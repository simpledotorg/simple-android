package org.simple.clinic.drugs

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
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.protocolv2.ProtocolDrug
import org.simple.clinic.user.UserSession
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class PrescriptionRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var repository: PrescriptionRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var faker: Faker

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun prescriptions_for_a_patient_should_exclude_soft_deleted_prescriptions() {
    val facilityUUID = testData.qaUserFacilityUuid()
    database.facilityDao().save(listOf(testData.facility(uuid = facilityUUID, syncStatus = SyncStatus.DONE)))

    val addressUuid = UUID.randomUUID()
    database.addressDao().save(testData.patientAddress(uuid = addressUuid))

    val patientUuid = UUID.randomUUID()
    database.patientDao().save(testData.patient(uuid = patientUuid, addressUuid = addressUuid))

    val protocolUuid = UUID.randomUUID()
    val amlodipine5mg = testData.protocolDrug(name = "Amlodipine", dosage = "5mg", protocolUuid = protocolUuid)
    val amlodipine10mg = testData.protocolDrug(name = "Amlodipine", dosage = "10mg", protocolUuid = protocolUuid)

    repository.savePrescription(patientUuid, amlodipine5mg).blockingAwait()

    val savedPrescriptions = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptions).hasSize(1)

    repository.savePrescription(patientUuid, amlodipine10mg)
        .andThen(repository.softDeletePrescription(savedPrescriptions.first().uuid))
        .blockingAwait()

    val savedPrescriptionsAfterDelete = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptionsAfterDelete).hasSize(1)
  }

  @Test
  fun soft_delete_prescription_should_update_timestamp_and_sync_status() {
    val patientUUID = UUID.randomUUID()

    val amlodipine5mg = testData.protocolDrug(name = "Amlodipine", dosage = "5mg")
    repository.savePrescription(patientUUID, amlodipine5mg).blockingAwait()

    val savedPrescriptions = repository.newestPrescriptionsForPatient(patientUUID).blockingFirst()
    assertThat(savedPrescriptions).hasSize(1)

    val prescription = savedPrescriptions[0]
    repository.softDeletePrescription(prescription.uuid).blockingAwait()

    val softDeletedPrescription = database.prescriptionDao().getOne(prescription.uuid)!!

    assertThat(softDeletedPrescription.updatedAt).isGreaterThan(prescription.updatedAt)
    assertThat(softDeletedPrescription.createdAt).isEqualTo(prescription.createdAt)
    assertThat(softDeletedPrescription.syncStatus).isEqualTo(SyncStatus.PENDING)
  }

  @Test
  fun prescriptions_should_be_overridable() {
    val prescription = testData.prescription(name = "Churro")
    database.prescriptionDao().save(listOf(prescription))

    val correctedPrescription = prescription.copy(name = "Amlodipine")
    database.prescriptionDao().save(listOf(correctedPrescription))

    val storedPrescription = database.prescriptionDao().getOne(correctedPrescription.uuid)!!
    assertThat(storedPrescription.name).isEqualTo(correctedPrescription.name)
  }
}
