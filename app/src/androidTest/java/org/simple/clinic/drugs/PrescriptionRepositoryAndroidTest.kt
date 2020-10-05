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
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.BD
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.OD
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Rules
import org.simple.clinic.util.TestUtcClock
import java.time.Duration
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
  fun updating_prescribed_drug_duration_should_work_correctly() {
    // given
    val durationToAdvanceBy = Duration.ofMinutes(10)
    val timestamps = Timestamps.create(clock)
    val prescribedDrug = TestData.prescription(
        uuid = UUID.fromString("c4b74e2b-2ea3-4c4d-a6ab-8e93faa66159"),
        name = "Taco",
        durationInDays = 30,
        timestamps = timestamps,
        syncStatus = SyncStatus.DONE
    )

    database.prescriptionDao().save(listOf(prescribedDrug))

    // when
    clock.advanceBy(durationToAdvanceBy)
    repository.updateDrugDuration(
        id = prescribedDrug.uuid,
        duration = Duration.ofDays(20)
    )

    // then
    val expectedPrescribedDrug = prescribedDrug.copy(
        durationInDays = 20,
        timestamps = timestamps.copy(updatedAt = prescribedDrug.updatedAt.plus(durationToAdvanceBy)),
        syncStatus = SyncStatus.PENDING
    )
    assertThat(database.prescriptionDao().getOne(prescribedDrug.uuid)).isEqualTo(expectedPrescribedDrug)
  }

  @Test
  fun updating_prescribed_drug_frequency_should_work_correctly() {
    // given
    val durationToAdvanceBy = Duration.ofMinutes(10)
    val timestamps = Timestamps.create(clock)
    val prescribedDrug = TestData.prescription(
        uuid = UUID.fromString("2f9daf84-50fa-4955-ab18-ea48d8fc9fe1"),
        name = "Taco",
        frequency = OD,
        timestamps = timestamps,
        syncStatus = SyncStatus.DONE
    )

    database.prescriptionDao().save(listOf(prescribedDrug))

    // when
    clock.advanceBy(durationToAdvanceBy)
    repository.updateDrugFrequency(
        id = prescribedDrug.uuid,
        drugFrequency = BD
    )

    // then
    val expectedPrescribedDrug = prescribedDrug.copy(
        frequency = BD,
        timestamps = timestamps.copy(updatedAt = prescribedDrug.updatedAt.plus(durationToAdvanceBy)),
        syncStatus = SyncStatus.PENDING
    )
    assertThat(database.prescriptionDao().getOne(prescribedDrug.uuid)).isEqualTo(expectedPrescribedDrug)
  }

  @Test
  fun adding_teleconsultation_id_to_prescribed_drugs_should_work_correctly() {
    // given
    val durationToAdvanceBy = Duration.ofMinutes(10)
    val patient1Uuid = UUID.fromString("fc9a1aba-2d97-4505-9aee-5580a4d2b335")
    val patient2Uuid = UUID.fromString("017853c0-b09a-4ce8-9b59-228f54c7e0e8")
    val teleconsultationId = UUID.fromString("35756e86-4b31-44cb-bd5f-cd3bc8ad2448")

    val prescribedDrug1 = TestData.prescription(
        uuid = UUID.fromString("4ba45cf0-b88c-4efd-8fb0-e46c3b2bb32e"),
        name = "Amlodipine",
        patientUuid = patient1Uuid,
        timestamps = Timestamps.create(clock),
        syncStatus = SyncStatus.DONE,
        teleconsultationId = null
    )

    val prescribedDrug2 = TestData.prescription(
        uuid = UUID.fromString("0a6e0070-0bec-427f-b02d-fe7bc7a1e299"),
        name = "Taco",
        patientUuid = patient1Uuid,
        frequency = MedicineFrequency.TDS,
        timestamps = Timestamps.create(clock),
        syncStatus = SyncStatus.DONE,
        teleconsultationId = null
    )

    val prescribedDrug3 = TestData.prescription(
        uuid = UUID.fromString("d3d23772-ec14-41c4-b1e0-9fff09a785b3"),
        name = "Metaformin",
        patientUuid = patient2Uuid,
        frequency = MedicineFrequency.TDS,
        timestamps = Timestamps.create(clock),
        syncStatus = SyncStatus.DONE,
        teleconsultationId = null
    )

    database.prescriptionDao().save(listOf(prescribedDrug1, prescribedDrug2, prescribedDrug3))

    // when
    clock.advanceBy(durationToAdvanceBy)
    repository.addTeleconsultationIdToDrugs(
        prescribedDrugs = listOf(prescribedDrug1, prescribedDrug2),
        teleconsultationId = teleconsultationId
    )

    // then
    val expectedPrescribedDrugs = listOf(
        prescribedDrug1.copy(
            teleconsultationId = teleconsultationId,
            timestamps = prescribedDrug1.timestamps.copy(updatedAt = prescribedDrug1.updatedAt.plus(durationToAdvanceBy)),
            syncStatus = SyncStatus.PENDING
        ),
        prescribedDrug2.copy(
            teleconsultationId = teleconsultationId,
            timestamps = prescribedDrug2.timestamps.copy(updatedAt = prescribedDrug2.updatedAt.plus(durationToAdvanceBy)),
            syncStatus = SyncStatus.PENDING
        )
    )
    assertThat(repository.newestPrescriptionsForPatientImmediate(patient1Uuid)).isEqualTo(expectedPrescribedDrugs)
    assertThat(repository.prescriptionImmediate(prescribedDrug3.uuid)).isEqualTo(prescribedDrug3)
  }

  @Test
  fun soft_deleting_prescriptions_should_work_correctly() {
    // given
    val durationToAdvanceBy = Duration.ofMinutes(20)
    val patientUuid = UUID.fromString("7437be8d-586b-4a58-9c61-1d54bd485606")
    val prescribedDrug1 = TestData.prescription(
        uuid = UUID.fromString("c9f561ee-2d4e-4a3c-a21a-2d68cb11e524"),
        patientUuid = patientUuid,
        timestamps = Timestamps.create(clock),
        syncStatus = SyncStatus.DONE,
        isDeleted = false
    )

    val prescribedDrug2 = TestData.prescription(
        uuid = UUID.fromString("8e5687d9-1aa4-4bc3-898c-7523f093062e"),
        patientUuid = patientUuid,
        timestamps = Timestamps.create(clock),
        syncStatus = SyncStatus.DONE,
        isDeleted = false
    )

    val prescribedDrug3 = TestData.prescription(
        uuid = UUID.fromString("4b1ce9c6-ea8c-46ee-be2c-6f5cd56f6686"),
        patientUuid = patientUuid,
        timestamps = Timestamps.create(clock),
        syncStatus = SyncStatus.DONE,
        isDeleted = false
    )

    repository.save(listOf(prescribedDrug1, prescribedDrug2, prescribedDrug3)).blockingAwait()

    // when
    clock.advanceBy(durationToAdvanceBy)
    repository.softDeletePrescriptions(listOf(prescribedDrug1, prescribedDrug3))

    // then
    assertThat(repository.newestPrescriptionsForPatientImmediate(patientUuid))
        .containsExactly(prescribedDrug2)
        .inOrder()
    assertThat(repository.prescriptionImmediate(prescribedDrug1.uuid)).isEqualTo(prescribedDrug1.copy(
        isDeleted = true,
        timestamps = prescribedDrug1.timestamps.copy(updatedAt = prescribedDrug1.updatedAt.plus(durationToAdvanceBy)),
        syncStatus = SyncStatus.PENDING
    ))
    assertThat(repository.prescriptionImmediate(prescribedDrug3.uuid)).isEqualTo(prescribedDrug3.copy(
        isDeleted = true,
        timestamps = prescribedDrug3.timestamps.copy(updatedAt = prescribedDrug3.updatedAt.plus(durationToAdvanceBy)),
        syncStatus = SyncStatus.PENDING
    ))
  }
}
