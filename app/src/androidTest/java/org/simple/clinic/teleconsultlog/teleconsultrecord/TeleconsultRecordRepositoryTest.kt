package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import org.simple.clinic.util.TestUtcClock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepositoryTest {

  @Inject
  lateinit var testUtcClock: TestUtcClock

  @Inject
  lateinit var teleconsultRecordRepository: TeleconsultRecordRepository

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    testUtcClock.setDate(LocalDate.parse("2018-01-01"))
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
        deletedAt = null
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
}
