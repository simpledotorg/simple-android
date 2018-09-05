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
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.protocol.ProtocolDrug
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
    database.facilityDao().save(listOf(
        Facility(
            facilityUUID,
            faker.company.name(),
            null,
            null,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            null,
            Instant.now(),
            Instant.now(),
            SyncStatus.DONE
        )
    ))

    val addressUuid = UUID.randomUUID()
    database.addressDao().save(
        PatientAddress(
            addressUuid,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            Instant.now(),
            Instant.now()
        )
    )

    val patientUuid = UUID.randomUUID()
    database.patientDao().save(
        Patient(
            patientUuid,
            addressUuid,
            faker.name.name(),
            faker.name.name(),
            Gender.FEMALE,
            LocalDate.parse("1947-08-15"),
            null,
            PatientStatus.ACTIVE,
            Instant.now(),
            Instant.now(),
            SyncStatus.DONE
        ))

    val protocolUuid = UUID.randomUUID()
    val amlodipine = ProtocolDrug(UUID.randomUUID(), "Amlodipine", rxNormCode = null, dosages = listOf("5mg", "10mg"), protocolUUID = protocolUuid)

    repository.savePrescription(patientUuid, amlodipine, "5mg").blockingAwait()

    val savedPrescriptions1 = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptions1).hasSize(1)

    repository.savePrescription(patientUuid, amlodipine, "10mg")
        .andThen(repository.softDeletePrescription(savedPrescriptions1[0].uuid))
        .blockingAwait()

    val savedPrescriptions2 = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptions2).hasSize(1)
  }

  @Test
  fun soft_delete_prescription_should_update_timestamp_and_sync_status() {
    val protocolUUID = UUID.randomUUID()
    val patientUUID = UUID.randomUUID()

    val drug = ProtocolDrug(UUID.randomUUID(), name = "Amlodipine", rxNormCode = null, dosages = listOf("5mg", "10mg"), protocolUUID = protocolUUID)

    repository.savePrescription(patientUUID, drug, "5mg").blockingAwait()

    val savedPrescriptions = repository.newestPrescriptionsForPatient(patientUUID).blockingFirst()
    assertThat(savedPrescriptions).hasSize(1)

    val prescription = savedPrescriptions[0]

    repository.softDeletePrescription(prescription.uuid).blockingAwait()

    val softDeletedPrescription = database.prescriptionDao().getOne(prescription.uuid)!!

    assertThat(softDeletedPrescription.updatedAt).isGreaterThan(prescription.updatedAt)
    assertThat(softDeletedPrescription.createdAt).isEqualTo(prescription.createdAt)
    assertThat(softDeletedPrescription.syncStatus).isEqualTo(SyncStatus.PENDING)
  }
}
