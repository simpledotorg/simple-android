package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.toUtcInstant
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordTest {

  @Inject
  lateinit var teleconsultRecordDao: TeleconsultRecord.RoomDao

  private val userClock = TestUserClock()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    teleconsultRecordDao.clear()
  }

  @Test
  fun teleconsultation_records_should_be_fetched_correctly() {

    // given
    val teleconsultRecordId1 = UUID.fromString("700ee55d-7f49-4bda-9a4a-c5ce903ce485")
    val teleconsultRecordId2 = UUID.fromString("f1ad859f-d076-4bae-b0f2-3fb23c60d880")
    val patientUuid1 = UUID.fromString("3c00cdf9-4304-4dc7-8d32-6fbd5cd8f14d")
    val patientUuid2 = UUID.fromString("a706dfbf-315a-4201-be42-10b784c6d0b4")
    val medicalOfficerUuid = UUID.fromString("7142092e-24b1-4757-b7b6-a00fbd60332b")
    val facilityId = UUID.fromString("f12c2316-85e4-466e-9c92-efa52605c6b9")
    val requesterId = UUID.fromString("7a131902-c214-4aa9-800f-201513d77fd7")
    val date = LocalDate.parse("2020-09-03")

    val teleconsultRecordInfo = TestData.teleconsultRecordInfo(
        recordedAt = Instant.parse("2020-09-03T00:00:00Z"),
        teleconsultationType = TeleconsultationType.Audio,
        patientTookMedicines = Answer.Yes,
        patientConsented = Answer.No,
        medicalOfficerNumber = "22222222"
    )

    val teleconsultRequestInfo = TestData.teleconsultRequestInfo(
        requesterId = requesterId,
        facilityId = facilityId,
        requestedAt = Instant.parse("2020-09-02T00:00:00Z"),
    )

    val teleconsultRecord1 = TestData.teleconsultRecord(
        id = teleconsultRecordId1,
        patientId = patientUuid1,
        medicalOfficerId = medicalOfficerUuid,
        teleconsultRecordInfo = teleconsultRecordInfo,
        teleconsultRequestInfo = teleconsultRequestInfo,
        timestamps = Timestamps(
            createdAt = date.toUtcInstant(userClock),
            updatedAt = date.toUtcInstant(userClock),
            deletedAt = null
        )
    )

    val teleconsultRecord2 = TestData.teleconsultRecord(
        id = teleconsultRecordId2,
        patientId = patientUuid2,
        medicalOfficerId = medicalOfficerUuid,
        teleconsultRecordInfo = teleconsultRecordInfo,
        teleconsultRequestInfo = teleconsultRequestInfo,
        timestamps = Timestamps(
            createdAt = date.toUtcInstant(userClock),
            updatedAt = date.toUtcInstant(userClock),
            deletedAt = null
        )
    )

    val expectedTeleconsultRecordList = listOf(
        teleconsultRecord1,
        teleconsultRecord2
    )

    teleconsultRecordDao.save(
        expectedTeleconsultRecordList
    )

    // when
    val teleconsultRecordReceived = teleconsultRecordDao.getAll()

    // then
    assertThat(teleconsultRecordReceived).isEqualTo(expectedTeleconsultRecordList)
  }
}
