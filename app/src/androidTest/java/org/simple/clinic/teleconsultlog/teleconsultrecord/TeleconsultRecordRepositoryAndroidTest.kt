package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepositoryAndroidTest {

  @Inject
  lateinit var teleconsultRecordRepository: TeleconsultRecordRepository

  @Inject
  lateinit var testUtcClock: TestUtcClock

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    testUtcClock.setDate(LocalDate.of(2020, Month.SEPTEMBER, 15))
  }

  @After
  fun tearDown() {
    teleconsultRecordRepository.clear()
  }

  private val patientUuid = UUID.fromString("3c00cdf9-4304-4dc7-8d32-6fbd5cd8f14d")
  private val teleconsultRecordId = UUID.fromString("700ee55d-7f49-4bda-9a4a-c5ce903ce485")
  private val medicalOfficerId = UUID.fromString("7142092e-24b1-4757-b7b6-a00fbd60332b")
  private val nurseId = UUID.fromString("c50367bc-eb28-48c1-add1-789d446fc718")
  private val facilityId = UUID.fromString("85712a0c-760a-4407-b9dc-ecfbdbe4d32d")


  @Test
  fun saving_teleconsult_records_with_prescribed_drugs_should_work_properly() {
    // given
    val teleconsultRecordId1 = UUID.fromString("7631538a-7510-4147-b239-1c56c7d2ef70")
    val teleconsultRecordId2 = UUID.fromString("9ee0bc81-26e6-4ced-9b33-fa76a0a995e3")
    val teleconsultRecordId3 = UUID.fromString("f1ad859f-d076-4bae-b0f2-3fb23c60d880")
    val timestamps = Timestamps.create(testUtcClock)

    val teleconsultRecord1 = TestData.teleconsultRecord(
        id = teleconsultRecordId1,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRecordInfo = TestData.teleconsultRecordInfo(
            recordedAt = Instant.parse("2020-09-14T00:00:00Z"),
            teleconsultationType = TeleconsultationType.Audio,
            patientTookMedicines = Yes,
            patientConsented = Yes,
            medicalOfficerNumber = null),
        timestamps = timestamps
    )


    val teleconsultRecord2 = TestData.teleconsultRecord(
        id = teleconsultRecordId2,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRecordInfo = TestData.teleconsultRecordInfo(
            recordedAt = Instant.parse("2020-09-14T00:00:00Z"),
            teleconsultationType = TeleconsultationType.Audio,
            patientTookMedicines = Yes,
            patientConsented = Yes,
            medicalOfficerNumber = null),
        timestamps = timestamps
    )

    val teleconsultRecord3 = TestData.teleconsultRecord(
        id = teleconsultRecordId3,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRecordInfo = TestData.teleconsultRecordInfo(
            recordedAt = Instant.parse("2020-09-14T00:00:00Z"),
            teleconsultationType = TeleconsultationType.Audio,
            patientTookMedicines = Yes,
            patientConsented = Yes,
            medicalOfficerNumber = null),
        timestamps = timestamps
    )

    val teleconsultRecords = listOf(teleconsultRecord1, teleconsultRecord2, teleconsultRecord3)

    teleconsultRecordRepository.save(teleconsultRecords).blockingAwait()

    // when
    val teleconsultRecordDetails = teleconsultRecordRepository
        .getTeleconsultRecord(teleconsultRecordId1)

    // then
    assertThat(teleconsultRecordDetails)
        .isEqualTo(teleconsultRecord1)
  }

  @Test
  fun creating_teleconsult_record_for_medical_officer_should_work_as_expected() {
    // given
    val teleconsultRecordId = UUID.fromString("700ee55d-7f49-4bda-9a4a-c5ce903ce485")
    val medicalOfficerRegistrationId = "1111111111111"

    val teleconsultRecordInfo = TestData.teleconsultRecordInfo(
        recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
        teleconsultationType = TeleconsultationType.Audio,
        patientTookMedicines = Yes,
        patientConsented = Yes,
        medicalOfficerNumber = medicalOfficerRegistrationId
    )
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = null,
        teleconsultRecordInfo = teleconsultRecordInfo,
        createdAt = Instant.now(testUtcClock),
        updatedAt = Instant.now(testUtcClock),
        deletedAt = null,
        syncStatus = SyncStatus.PENDING
    )

    teleconsultRecordRepository.createTeleconsultRecordForMedicalOfficer(
        teleconsultRecordId = teleconsultRecordId,
        patientUuid = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRecordInfo = teleconsultRecordInfo
    )

    // when
    val teleconsultRecordDetails = teleconsultRecordRepository.getTeleconsultRecord(teleconsultRecordId)

    // then
    assertThat(teleconsultRecordDetails).isEqualTo(teleconsultRecord)
  }

  @Test
  fun updating_the_medical_officer_number_should_work_correctly() {
    // given
    val medicalOfficerNumber = "ABC123455"
    val teleconsultRecord = TestData.teleconsultRecord(
        id = UUID.fromString("9e22cb89-ce9f-4bc2-bc07-b69482508411"),
        teleconsultRecordInfo = TestData.teleconsultRecordInfo(
            recordedAt = Instant.now(testUtcClock),
            teleconsultationType = TeleconsultationType.Audio,
            patientTookMedicines = Yes,
            patientConsented = Yes,
            medicalOfficerNumber = null
        ),
        timestamps = Timestamps(
            createdAt = Instant.now(testUtcClock),
            updatedAt = Instant.now(testUtcClock),
            deletedAt = null
        ),
        syncStatus = SyncStatus.PENDING
    )

    teleconsultRecordRepository.createTeleconsultRecordForMedicalOfficer(
        teleconsultRecordId = teleconsultRecord.id,
        patientUuid = teleconsultRecord.patientId,
        medicalOfficerId = teleconsultRecord.medicalOfficerId,
        teleconsultRecordInfo = teleconsultRecord.teleconsultRecordInfo!!
    )

    // when
    testUtcClock.advanceBy(Duration.ofMinutes(2))
    teleconsultRecordRepository.updateMedicalRegistrationId(
        teleconsultRecordId = teleconsultRecord.id,
        medicalOfficerNumber = medicalOfficerNumber
    )

    // then
    val expectedTeleconsultRecord = teleconsultRecord.copy(
        teleconsultRecordInfo = teleconsultRecord.teleconsultRecordInfo!!.copy(medicalOfficerNumber = medicalOfficerNumber),
        timestamp = teleconsultRecord.timestamp.copy(updatedAt = teleconsultRecord.timestamp.updatedAt.plus(Duration.ofMinutes(2))),
        syncStatus = SyncStatus.PENDING
    )
    assertThat(teleconsultRecordRepository.getTeleconsultRecord(teleconsultRecord.id))
        .isEqualTo(expectedTeleconsultRecord)
  }

  @Test
  fun creating_teleconsult_request_for_nurse_should_work_correctly() {
    // given
    val teleconsultRequestInfo = TestData.teleconsultRequestInfo(
        requestedAt = Instant.now(testUtcClock),
        requesterId = nurseId,
        facilityId = facilityId
    )

    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfo,
        teleconsultRecordInfo = null,
        createdAt = Instant.now(testUtcClock),
        updatedAt = Instant.now(testUtcClock),
        deletedAt = null,
        syncStatus = SyncStatus.PENDING
    )

    teleconsultRecordRepository.createTeleconsultRequestForNurse(
        teleconsultRecordId = teleconsultRecordId,
        patientUuid = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfo
    )

    // when
    val teleconsultRecordDetails = teleconsultRecordRepository.getTeleconsultRecord(teleconsultRecordId)

    // then
    assertThat(teleconsultRecordDetails).isEqualTo(teleconsultRecord)
  }

  @Test
  fun updating_requester_completion_status_should_work_correctly() {
    // given
    val teleconsultRecord = TestData.teleconsultRecord(
        id = UUID.fromString("9e22cb89-ce9f-4bc2-bc07-b69482508411"),
        teleconsultRequestInfo = TestData.teleconsultRequestInfo(
            requesterId = UUID.fromString("10a74617-78a9-4d54-9525-7ba47958dbde"),
            facilityId = UUID.fromString("c8bd5e29-a428-42c1-9cce-e552c3079e4a"),
            requestedAt = Instant.now(testUtcClock),
            requesterCompletionStatus = null
        ),
        timestamps = Timestamps(
            createdAt = Instant.now(testUtcClock),
            updatedAt = Instant.now(testUtcClock),
            deletedAt = null
        ),
        syncStatus = SyncStatus.DONE
    )

    teleconsultRecordRepository.createTeleconsultRequestForNurse(
        teleconsultRecordId = teleconsultRecord.id,
        patientUuid = teleconsultRecord.patientId,
        medicalOfficerId = teleconsultRecord.medicalOfficerId,
        teleconsultRequestInfo = teleconsultRecord.teleconsultRequestInfo!!
    )

    // when
    testUtcClock.advanceBy(Duration.ofMinutes(2))
    teleconsultRecordRepository.updateRequesterCompletionStatus(
        teleconsultRecordId = teleconsultRecord.id,
        teleconsultStatus = TeleconsultStatus.Yes
    )

    // then
    val expectedTeleconsultRecord = teleconsultRecord.copy(
        teleconsultRequestInfo = teleconsultRecord.teleconsultRequestInfo!!.copy(requesterCompletionStatus = TeleconsultStatus.Yes),
        timestamp = teleconsultRecord.timestamp.copy(updatedAt = teleconsultRecord.timestamp.updatedAt.plus(Duration.ofMinutes(2))),
        syncStatus = SyncStatus.PENDING
    )
    assertThat(teleconsultRecordRepository.getTeleconsultRecord(teleconsultRecord.id))
        .isEqualTo(expectedTeleconsultRecord)
  }

  @Test
  fun getting_patient_latest_teleconsult_record_should_work_correctly() {
    // given
    val teleconsultRequestInfo = TestData.teleconsultRequestInfo(
        requestedAt = Instant.now(testUtcClock),
        requesterId = nurseId,
        facilityId = facilityId
    )

    val teleconsultRecordDetails = TestData.teleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfo,
        teleconsultRecordInfo = null,
        createdAt = Instant.now(testUtcClock),
        updatedAt = Instant.now(testUtcClock),
        deletedAt = null,
        syncStatus = SyncStatus.PENDING
    )

    teleconsultRecordRepository.save(listOf(teleconsultRecordDetails)).blockingAwait()

    // when
    val teleconsultRecord = teleconsultRecordRepository.getPatientTeleconsultRecord(patientUuid = patientUuid)

    // then
    assertThat(teleconsultRecord).isEqualTo(teleconsultRecordDetails)
  }
}
