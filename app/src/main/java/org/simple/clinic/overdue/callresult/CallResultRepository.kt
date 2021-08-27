package org.simple.clinic.overdue.callresult

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SynceableRepository
import java.util.UUID
import javax.inject.Inject

class CallResultRepository @Inject constructor(
    private val callResultDao: CallResult.RoomDao
) : SynceableRepository<CallResult, CallResultPayload> {

  override fun save(records: List<CallResult>): Completable {
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {

  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {

  }

  override fun mergeWithLocalData(payloads: List<CallResultPayload>) {

  }

  override fun recordCount(): Observable<Int> {

  }

  override fun pendingSyncRecordCount(): Observable<Int> {

  }

  override fun pendingSyncRecords(limit: Int, offset: Int): List<CallResult> {

  }
}
