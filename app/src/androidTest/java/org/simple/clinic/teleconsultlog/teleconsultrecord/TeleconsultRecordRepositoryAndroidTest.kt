package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.util.TestUtcClock
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepositoryAndroidTest {

  @Inject
  lateinit var repository: TeleconsultRecordRepository

  @Inject
  lateinit var testUtcClock: TestUtcClock

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    testUtcClock.setDate(LocalDate.of(2020, Month.SEPTEMBER, 15))
  }

  @After
  fun tearDown() {
    repository.clear()
  }

  @Test
  fun saving_teleconsult_records_with_prescribed_drugs_should_work_properly() {
    // given
    val teleconsultRecordId1 = UUID.fromString("7631538a-7510-4147-b239-1c56c7d2ef70")
    val teleconsultRecordId2 = UUID.fromString("9ee0bc81-26e6-4ced-9b33-fa76a0a995e3")
    val teleconsultRecordId3 = UUID.fromString("f1ad859f-d076-4bae-b0f2-3fb23c60d880")
    val patientUuid = UUID.fromString("91f230a6-e67f-428e-95f8-6090415a5c4e")
    val medicalOfficerId = UUID.fromString("5e2a46b8-6d77-47fa-9538-281e4992cb46")
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

    repository.save(teleconsultRecords).blockingAwait()

    // when
    val teleconsultRecordDetails = repository
        .getTeleconsultRecord(teleconsultRecordId1)

    // then
    assertThat(teleconsultRecordDetails)
        .isEqualTo(teleconsultRecord1)
  }
}
