package org.simple.clinic.questionnaireresponse

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponsePayload
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class QuestionnaireResponseRepository @Inject constructor(
    val dao: QuestionnaireResponse.RoomDao,
    val utcClock: UtcClock
) : SynceableRepository<QuestionnaireResponse, QuestionnaireResponsePayload> {

  fun save(record: QuestionnaireResponse) {
    save(listOf(record))
  }

  override fun save(records: List<QuestionnaireResponse>) {
    dao.save(records)
  }

  fun questionnaireResponsesByType(questionnaireType: QuestionnaireType): Observable<List<QuestionnaireResponse>> {
    return dao
        .getByQuestionnaireType(questionnaireType)
        .toObservable()
  }

  fun questionnaireResponse(uuid: UUID): QuestionnaireResponse? {
    return dao.getOne(uuid)
  }

  fun updateQuestionnaireResponse(
      loggedInUser: User,
      questionnaireResponse: QuestionnaireResponse
  ) {
    val updatedQuestionnaireResponse = questionnaireResponse.copy(
        lastUpdatedByUserId = loggedInUser.uuid,
        timestamps = questionnaireResponse.timestamps.copy(
            updatedAt = Instant.now(utcClock)
        ),
        syncStatus = SyncStatus.PENDING
    )

    dao.updateQuestionnaireResponse(updatedQuestionnaireResponse)
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
