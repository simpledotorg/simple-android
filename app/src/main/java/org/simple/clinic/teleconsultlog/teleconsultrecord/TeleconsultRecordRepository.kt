package org.simple.clinic.teleconsultlog.teleconsultrecord

import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.UtcClock
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepository @Inject constructor(
    private val uuidGenerator: UuidGenerator,
    private val teleconsultRecordDao: TeleconsultRecord.RoomDao,
    private val teleconsultRecordWithPrescribedDrugsDao: TeleconsultRecordWithPrescribedDrugs.RoomDao,
    private val utcClock: UtcClock
) {

  fun getTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId: UUID): TeleconsultRecordWithPrescribedDrugs? {
    return teleconsultRecordWithPrescribedDrugsDao.getPrescribedUuidForTeleconsultRecordUuid(teleconsultRecordId)
  }

  fun createTeleconsultRecord(
      teleconsultRecordId: UUID = uuidGenerator.v4(),
      teleconsultationType: TeleconsultationType,
      patientConsented: Answer,
      patientTookMedicine: Answer,
      patientUuid: UUID,
      medicalOfficerId: UUID,
      medicalOfficerRegistrationId: String?
  ) {
    val teleconsultRecordInfo = TeleconsultRecordInfo(
        recordedAt = Instant.now(utcClock),
        teleconsultationType = teleconsultationType,
        patientConsented = patientConsented,
        patientTookMedicines = patientTookMedicine,
        medicalOfficerNumber = medicalOfficerRegistrationId
    )

    val teleconsultRecord = TeleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = null,
        teleconsultRecordInfo = teleconsultRecordInfo,
        timestamp = Timestamps.create(utcClock)
    )

    teleconsultRecordDao.save(listOf(teleconsultRecord))
  }
}
