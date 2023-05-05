package org.simple.clinic.questionnaire

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.questionnaire.sync.QuestionnairePayload
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

@AppScope
class QuestionnaireRepository @Inject constructor(
    private val dao: Questionnaire.RoomDao
) : SynceableRepository<Questionnaire, QuestionnairePayload> {

  override fun save(records: List<Questionnaire>) {
    saveRecords(records)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    // no-op
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    // no-op
  }

  override fun mergeWithLocalData(payloads: List<QuestionnairePayload>) {
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

  override fun pendingSyncRecords(limit: Int, offset: Int): List<Questionnaire> {
    return emptyList()
  }

  private fun saveRecords(records: List<Questionnaire>) {
    dao.save(records)
  }

  fun questionnaires(): Observable<List<Questionnaire>> {
    return dao.getAll().toObservable()
  }

  fun questionnaireByType(type: QuestionnaireType): Questionnaire {
    return dao.getByQuestionnaireType(type)
  }
}
