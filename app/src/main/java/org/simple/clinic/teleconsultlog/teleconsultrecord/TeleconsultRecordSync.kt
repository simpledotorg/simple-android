package org.simple.clinic.teleconsultlog.teleconsultrecord

import io.reactivex.Completable
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import javax.inject.Inject
import javax.inject.Named

class TeleconsultRecordSync @Inject constructor(
    private val teleconsultRecordApi: TeleconsultRecordApi,
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val syncCoordinator: SyncCoordinator,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  override val name: String = "TeleconsultRecord"

  override val requiresSyncApprovedUser: Boolean = true

  override fun sync(): Completable {
    return Completable.fromAction { push() } // Api endpoint is push only
  }

  override fun push() {
    syncCoordinator.push(
        teleconsultRecordRepository
    ) { teleconsultRecordApi.push(toRequest(it)).execute().read()!! }
  }

  override fun pull() {
    /* Nothing to do here */
  }

  override fun syncConfig(): SyncConfig = config

  private fun toRequest(teleconsultRecord: List<TeleconsultRecord>): TeleconsultPushRequest {
    val payloads = teleconsultRecord.map { recordToPayload(it) }
    return TeleconsultPushRequest(payloads)
  }

  private fun recordToPayload(teleconsultRecord: TeleconsultRecord): TeleconsultRecordPayload {
    val teleconsultRequestInfoPayload = getTeleconsultRequestInfoPayload(teleconsultRecord.teleconsultRequestInfo)
    val teleconsultRecordInfoPayload = getTeleconsultRecordInfoPayload(teleconsultRecord.teleconsultRecordInfo)

    return TeleconsultRecordPayload(
        id = teleconsultRecord.id,
        patientId = teleconsultRecord.patientId,
        medicalOfficerId = teleconsultRecord.medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfoPayload,
        teleconsultRecordInfo = teleconsultRecordInfoPayload,
        createdAt = teleconsultRecord.timestamp.createdAt,
        updatedAt = teleconsultRecord.timestamp.updatedAt,
        deletedAt = teleconsultRecord.timestamp.deletedAt
    )
  }

  private fun getTeleconsultRecordInfoPayload(teleconsultRecordInfo: TeleconsultRecordInfo?): TeleconsultRecordInfoPayload? {
    return if (teleconsultRecordInfo != null) {
      TeleconsultRecordInfoPayload(
          recordedAt = teleconsultRecordInfo.recordedAt,
          teleconsultationType = teleconsultRecordInfo.teleconsultationType,
          patientTookMedicines = teleconsultRecordInfo.patientTookMedicines,
          patientConsented = teleconsultRecordInfo.patientConsented,
          medicalOfficerNumber = teleconsultRecordInfo.medicalOfficerNumber.orEmpty(),
      )
    } else {
      null
    }
  }

  private fun getTeleconsultRequestInfoPayload(teleconsultRequestInfo: TeleconsultRequestInfo?): TeleconsultRequestInfoPayload? {
    return if (teleconsultRequestInfo != null) {
      TeleconsultRequestInfoPayload(
          requesterId = teleconsultRequestInfo.requesterId,
          facilityId = teleconsultRequestInfo.facilityId,
          requestedAt = teleconsultRequestInfo.requestedAt
      )
    } else {
      null
    }
  }
}
