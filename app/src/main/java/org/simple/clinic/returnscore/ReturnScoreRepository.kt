package org.simple.clinic.returnscore

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.returnscore.sync.ReturnScorePayload
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

@AppScope
class ReturnScoreRepository @Inject constructor(
    private val dao: ReturnScore.RoomDao
) : SynceableRepository<ReturnScore, ReturnScorePayload> {

  override fun save(records: List<ReturnScore>) {
    saveRecords(records)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    // no-op
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    // no-op
  }

  override fun mergeWithLocalData(payloads: List<ReturnScorePayload>) {
    val records = payloads
        .map { it.toDatabaseModel() }

    saveRecords(records)
  }

  override fun recordCount(): Observable<Int> {
    return dao.count()
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return Observable.just(0)
  }

  override fun pendingSyncRecords(limit: Int, offset: Int): List<ReturnScore> {
    return emptyList()
  }

  private fun saveRecords(records: List<ReturnScore>) {
    dao.save(records)
  }

  fun returnScores(): Observable<List<ReturnScore>> {
    return dao.getAll().toObservable()
  }

  fun returnScoresImmediate(): List<ReturnScore> {
    return dao.getAllImmediate()
  }

  fun returnScoresByType(type: ScoreType): Observable<List<ReturnScore>> {
    return dao.getByScoreType(type).toObservable()
  }
}
