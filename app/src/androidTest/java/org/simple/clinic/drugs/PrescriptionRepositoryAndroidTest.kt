package org.simple.clinic.drugs

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Rules
import org.simple.clinic.util.TestUtcClock
import java.time.LocalDate
import java.time.Month
import java.util.UUID
import javax.inject.Inject


class PrescriptionRepositoryAndroidTest {

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var repository: PrescriptionRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facility: Facility

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.of(2000, Month.JANUARY, 1))
  }

  @Test
  fun prescriptions_for_a_patient_should_exclude_soft_deleted_prescriptions() {
    database.facilityDao().save(listOf(testData.facility(uuid = facility.uuid, syncStatus = SyncStatus.DONE)))

    val addressUuid = UUID.randomUUID()
    database.addressDao().save(testData.patientAddress(uuid = addressUuid))

    val patientUuid = UUID.randomUUID()
    database.patientDao().save(testData.patient(uuid = patientUuid, addressUuid = addressUuid))

    val protocolUuid = UUID.randomUUID()
    val amlodipine5mg = testData.protocolDrug(name = "Amlodipine", dosage = "5mg", protocolUuid = protocolUuid)
    val amlodipine10mg = testData.protocolDrug(name = "Amlodipine", dosage = "10mg", protocolUuid = protocolUuid)

    val uuidOfFirstPrescribedDrug = UUID.fromString("ff3bed7a-e8aa-41a4-a205-cbd10e2ab754")
    repository.savePrescription(
        uuid = uuidOfFirstPrescribedDrug,
        patientUuid = patientUuid,
        drug = amlodipine5mg,
        facility = facility
    ).blockingAwait()

    val savedPrescriptions = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptions).hasSize(1)

    val uuidOfSecondPrescribedDrug = UUID.fromString("cdb88cdf-b903-4a85-b2f1-b167a735607f")
    repository
        .savePrescription(
            uuid = uuidOfSecondPrescribedDrug,
            patientUuid = patientUuid,
            drug = amlodipine10mg,
            facility = facility
        )
        .andThen(repository.softDeletePrescription(savedPrescriptions.first().uuid))
        .blockingAwait()

    val savedPrescriptionsAfterDelete = repository.newestPrescriptionsForPatient(patientUuid).blockingFirst()
    assertThat(savedPrescriptionsAfterDelete.first().uuid).isEqualTo(uuidOfSecondPrescribedDrug)
  }

  @Test
  fun soft_delete_prescription_should_update_timestamp_and_sync_status() {
    val patientUUID = UUID.randomUUID()

    val amlodipine5mg = testData.protocolDrug(name = "Amlodipine", dosage = "5mg")
    repository.savePrescription(
        uuid = UUID.fromString("64f9b33d-eed3-4cf9-be17-6a9e430e882d"),
        patientUuid = patientUUID,
        drug = amlodipine5mg,
        facility = facility
    ).blockingAwait()

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

  @Test
  fun getting_prescribed_drugs_uuid_for_patient_should_work_correctly() {
    // given
    val patient1Uuid = UUID.fromString("a1b8d50c-1d7b-4428-9b3c-3507cd7b723d")
    val patient2Uuid = UUID.fromString("c4b21e94-079b-49db-b70c-5b4d500d6cae")

    val prescribedDrug1Uuid = UUID.fromString("a6269313-90f0-47be-b23d-615d2ee0956a")
    val prescribedDrug1 = TestData.prescription(
        uuid = prescribedDrug1Uuid,
        patientUuid = patient1Uuid
    )

    val prescribedDrug2Uuid = UUID.fromString("cfcb33d1-7fac-40fa-a335-39f5f3099fe0")
    val prescribedDrug2 = TestData.prescription(
        uuid = prescribedDrug2Uuid,
        patientUuid = patient1Uuid
    )

    val prescribedDrug3Uuid = UUID.fromString("6fe649ef-9edb-450f-9037-1bb5d3a0420b")
    val prescribedDrug3 = TestData.prescription(
        uuid = prescribedDrug3Uuid,
        patientUuid = patient2Uuid
    )

    repository.save(listOf(prescribedDrug1, prescribedDrug2, prescribedDrug3)).blockingAwait()

    // when
    val prescribedDrugsUuidForPatient = repository.prescribedDrugsUuidForPatient(patient1Uuid)

    // then
    assertThat(prescribedDrugsUuidForPatient).isEqualTo(listOf(prescribedDrug1Uuid, prescribedDrug2Uuid))
  }
}
