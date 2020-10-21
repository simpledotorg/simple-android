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
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import org.simple.clinic.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepositoryTest {

  @Inject
  lateinit var testUtcClock: TestUtcClock

  @Inject
  lateinit var teleconsultRecordRepository: TeleconsultRecordRepository

  @Inject
  lateinit var appDatabase: org.simple.clinic.AppDatabase

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    testUtcClock.setDate(LocalDate.parse("2018-01-01"))
  }

  @After
  fun tearDown() {
    appDatabase.clearAllTables()
  }

  @Test
  fun creating_teleconsult_record_for_MO_should_work_as_expected() {
    // given
    val teleconsultRecordId = UUID.fromString("700ee55d-7f49-4bda-9a4a-c5ce903ce485")
    val patientUuid = UUID.fromString("3c00cdf9-4304-4dc7-8d32-6fbd5cd8f14d")
    val medicalOfficerUuid = UUID.fromString("7142092e-24b1-4757-b7b6-a00fbd60332b")
    val medicalOfficerRegistrationId = "1111111111111"

    val teleconsultRecordInfo = TestData.teleconsultRecordInfo(
        recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
        teleconsultationType = Audio,
        patientTookMedicines = Yes,
        patientConsented = Yes,
        medicalOfficerNumber = medicalOfficerRegistrationId
    )
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerUuid,
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
        medicalOfficerId = medicalOfficerUuid,
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
            teleconsultationType = Audio,
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
    val teleconsultRecordId = UUID.fromString("700ee55d-7f49-4bda-9a4a-c5ce903ce485")
    val patientUuid = UUID.fromString("3c00cdf9-4304-4dc7-8d32-6fbd5cd8f14d")
    val medicalOfficerUuid = UUID.fromString("7142092e-24b1-4757-b7b6-a00fbd60332b")
    val nurseId = UUID.fromString("c50367bc-eb28-48c1-add1-789d446fc718")
    val facilityId = UUID.fromString("85712a0c-760a-4407-b9dc-ecfbdbe4d32d")

    val teleconsultRequestInfo = TestData.teleconsultRequestInfo(
        requestedAt = Instant.now(testUtcClock),
        requesterId = nurseId,
        facilityId = facilityId
    )
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerUuid,
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
        medicalOfficerId = medicalOfficerUuid,
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
}
