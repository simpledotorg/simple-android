package org.simple.clinic.teleconsultlog.teleconsultrecord

import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.UtcClock
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordRepository @Inject constructor(
    private val teleconsultRecordDao: TeleconsultRecord.RoomDao,
    private val teleconsultRecordWithPrescribedDrugsDao: TeleconsultRecordWithPrescribedDrugs.RoomDao,
    private val teleconsultPrescribedDrugDao: TeleconsultRecordPrescribedDrug.RoomDao,
    private val utcClock: UtcClock
) {

  fun getTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId: UUID): TeleconsultRecordWithPrescribedDrugs? {
    return teleconsultRecordWithPrescribedDrugsDao.getCompleteTeleconsultLog(teleconsultRecordId)
  }

  fun createTeleconsultRecordForMedicalOfficer(
      teleconsultRecordId: UUID,
      patientUuid: UUID,
      medicalOfficerId: UUID,
      teleconsultRecordInfo: TeleconsultRecordInfo
  ) {
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

  fun saveTeleconsultPrescribedDrug(
      teleconsultRecordId: UUID,
      prescribedDrugsUuids: List<UUID>
  ) {
    val teleconsultPrescribedDrugs = prescribedDrugsUuids.map { drugUuid ->
      TeleconsultRecordPrescribedDrug(
          teleconsultRecordId = teleconsultRecordId,
          prescribedDrugUuid = drugUuid
      )
    }
    teleconsultPrescribedDrugDao.save(teleconsultPrescribedDrugs)
  }
}
