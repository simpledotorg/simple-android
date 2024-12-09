package org.simple.clinic.patientattribute

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patientattribute.sync.PatientAttributePayload
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.util.UtcClock
import java.util.UUID
import javax.inject.Inject

@AppScope
class PatientAttributeRepository @Inject constructor(
    val dao: PatientAttribute.RoomDao,
    private val utcClock: UtcClock
) : SynceableRepository<PatientAttribute, PatientAttributePayload> {
  fun save(
      reading: BMIReading,
      patientUuid: UUID,
      loggedInUserUuid: UUID,
      uuid: UUID,
  ) {
    val patientAttribute = PatientAttribute(
        uuid = uuid,
        patientUuid = patientUuid,
        userUuid = loggedInUserUuid,
        reading = reading,
        timestamps = Timestamps.create(utcClock),
        syncStatus = PENDING
    )
    dao.saveAttribute(patientAttribute)
  }

  override fun save(records: List<PatientAttribute>) {
    dao.saveAttributes(records)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    dao.updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    dao.updateSyncStatusForIds(ids, to)
  }

  override fun recordCount(): Observable<Int> = dao.count().toObservable()

  override fun pendingSyncRecordCount(): Observable<Int> =
      dao.countWithStatus(PENDING).toObservable()

  override fun pendingSyncRecords(limit: Int, offset: Int): List<PatientAttribute> {
    return dao
        .recordsWithSyncStatusBatched(
            syncStatus = PENDING,
            limit = limit,
            offset = offset
        )
  }

  override fun mergeWithLocalData(payloads: List<PatientAttributePayload>) {
    val dirtyRecords = dao.recordIdsWithSyncStatus(PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { it.toDatabaseModel(SyncStatus.DONE) }

    dao.saveAttributes(payloadsToSave)
  }

  fun getPatientAttributeImmediate(patientUuid: UUID): PatientAttribute? {
    return dao.patientAttributeImmediate(patientUuid)
  }
}
