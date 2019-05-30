package org.simple.clinic.drugs

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class PrescriptionRepositoryAndroidTest {

  @Inject
  lateinit var clock: UtcClock

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

  private val authenticationRule = AuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  private val testUtcClock: TestUtcClock
    get() = clock as TestUtcClock

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    (clock as TestUtcClock).setYear(2000)
  }

  @After
  fun tearDown() {
    (clock as TestUtcClock).resetToEpoch()
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

  @Test
  fun updating_a_prescription_should_update_it_correctly() {
    val prescription = testData.prescription(
        name = "Atenolol",
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock),
        syncStatus = SyncStatus.DONE)
    database.prescriptionDao().save(listOf(prescription))

    val correctedPrescription = prescription.copy(name = "Amlodipine")

    val durationToAdvanceBy = Duration.ofMinutes(15L)
    testUtcClock.advanceBy(durationToAdvanceBy)

    repository.updatePrescription(correctedPrescription).blockingAwait()

    val expected = prescription.copy(
        name = "Amlodipine",
        updatedAt = prescription.updatedAt.plus(durationToAdvanceBy),
        syncStatus = SyncStatus.PENDING
    )

    val storedPrescription = database.prescriptionDao().getOne(correctedPrescription.uuid)!!
    assertThat(storedPrescription).isEqualTo(expected)
  }

  @Test
  fun querying_whether_prescription_for_patient_has_changed_should_work_as_expected() {
    fun hasPrescriptionForPatientChangedSince(patientUuid: UUID, since: Instant): Boolean {
      return repository.hasPrescriptionForPatientChangedSince(patientUuid, since).blockingFirst()
    }

    fun setPrescribedDrugSyncStatusToDone(prescribedDrug: UUID) {
      repository.setSyncStatus(listOf(prescribedDrug), SyncStatus.DONE).blockingAwait()
    }

    val patientUuid = UUID.randomUUID()
    val now = Instant.now(clock)
    val oneSecondEarlier = now.minus(Duration.ofSeconds(1))
    val fiftyNineSecondsLater = now.plus(Duration.ofSeconds(59))
    val oneMinuteLater = now.plus(Duration.ofMinutes(1))

    val prescribedDrug1ForPatient = testData.prescription(
        patientUuid = patientUuid,
        syncStatus = SyncStatus.PENDING,
        updatedAt = now
    )
    val prescribedDrug2ForPatient = testData.prescription(
        patientUuid = patientUuid,
        syncStatus = SyncStatus.PENDING,
        updatedAt = oneMinuteLater
    )
    val prescribedDrugForSomeOtherPatient = testData.prescription(
        patientUuid = UUID.randomUUID(),
        syncStatus = SyncStatus.PENDING,
        updatedAt = now
    )

    repository.save(listOf(prescribedDrug1ForPatient, prescribedDrug2ForPatient, prescribedDrugForSomeOtherPatient)).blockingAwait()
    assertThat(hasPrescriptionForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()
    assertThat(hasPrescriptionForPatientChangedSince(patientUuid, now)).isTrue()
    assertThat(hasPrescriptionForPatientChangedSince(patientUuid, fiftyNineSecondsLater)).isTrue()
    assertThat(hasPrescriptionForPatientChangedSince(patientUuid, oneMinuteLater)).isFalse()

    setPrescribedDrugSyncStatusToDone(prescribedDrug2ForPatient.uuid)
    assertThat(hasPrescriptionForPatientChangedSince(patientUuid, fiftyNineSecondsLater)).isFalse()
    assertThat(hasPrescriptionForPatientChangedSince(patientUuid, oneSecondEarlier)).isTrue()

    setPrescribedDrugSyncStatusToDone(prescribedDrug1ForPatient.uuid)
    assertThat(hasPrescriptionForPatientChangedSince(patientUuid, oneSecondEarlier)).isFalse()
    assertThat(hasPrescriptionForPatientChangedSince(prescribedDrugForSomeOtherPatient.patientUuid, oneSecondEarlier)).isTrue()

    setPrescribedDrugSyncStatusToDone(prescribedDrugForSomeOtherPatient.uuid)
    assertThat(hasPrescriptionForPatientChangedSince(prescribedDrugForSomeOtherPatient.patientUuid, oneSecondEarlier)).isFalse()
  }
}
