package org.simple.clinic.questionnaireResponse

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

@AppScope
class QuestionnaireResponseRepository @Inject constructor(
    val dao: QuestionnaireResponse.RoomDao,
) : SynceableRepository<QuestionnaireResponse, QuestionnaireResponsePayload> {

  fun save(record: QuestionnaireResponse) {
    save(listOf(record))
  }

  override fun save(records: List<QuestionnaireResponse>) {
    dao.save(records)
  }

  fun questionnaireResponse(uuid: UUID): QuestionnaireResponse? {
    return dao.getOne(uuid)
  }

  fun recordsWithSyncStatus(syncStatus: SyncStatus): List<QuestionnaireResponse> {
    return dao.withSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    dao.updateSyncStatus(oldStatus = from, newStatus = to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    dao.updateSyncStatusForIds(uuids = ids, newStatus = to)
  }

  override fun mergeWithLocalData(payloads: List<QuestionnaireResponsePayload>) {
    val dirtyRecords = dao.recordIdsWithSyncStatus(SyncStatus.PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { it.toDatabaseModel(SyncStatus.DONE) }

    dao.save(payloadsToSave)
  }

  override fun recordCount(): Observable<Int> =
      dao.count().toObservable()

  override fun pendingSyncRecordCount(): Observable<Int> =
      dao.countWithStatus(SyncStatus.PENDING).toObservable()

  override fun pendingSyncRecords(limit: Int, offset: Int): List<QuestionnaireResponse> {
    return dao
        .recordsWithSyncStatusBatched(
            syncStatus = SyncStatus.PENDING,
            limit = limit,
            offset = offset
        )
  }
}
