package org.simple.clinic.overdue.communication

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.UserSession
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class CommunicationRepository @Inject constructor(
    val dao: Communication.RoomDao,
    val userSession: UserSession
) : SynceableRepository<Communication, CommunicationPayload> {

  fun create(appointmentUuid: UUID, type: Communication.Type, result: Communication.Result): Completable {
    return userSession.loggedInUser()
        .take(1)
        .flatMap { (user) ->
          when {
            user != null -> Observable.just(user)
            else -> Observable.error(AssertionError("User isn't logged in yet"))
          }
        }
        .map { user ->
          Communication(
              uuid = UUID.randomUUID(),
              appointmentUuid = appointmentUuid,
              userUuid = user.uuid,
              type = type,
              result = result,
              syncStatus = SyncStatus.PENDING,
              createdAt = Instant.now(),
              updatedAt = Instant.now(),
              deletedAt = null)
        }
        .flatMapCompletable { save(listOf(it)) }
  }

  override fun save(records: List<Communication>): Completable {
    return Completable.fromAction {
      dao.save(records)
    }
  }

  override fun recordCount(): Observable<Int> {
    return dao.count().toObservable()
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<Communication>> {
    return dao.recordsWithSyncStatus(syncStatus).firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return Completable.fromAction { dao.updateSyncStatus(from, to) }
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    if (ids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction { dao.updateSyncStatus(ids, to) }
  }

  override fun mergeWithLocalData(payloads: List<CommunicationPayload>): Completable {
    val newOrUpdatedCommunications = payloads
        .filter { payload: CommunicationPayload ->
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { toDatabaseModel(it, SyncStatus.DONE) }
        .toList()

    return Completable.fromAction { dao.save(newOrUpdatedCommunications) }
  }

  private fun toDatabaseModel(payload: CommunicationPayload, syncStatus: SyncStatus): Communication {
    return payload.run {
      Communication(
          uuid = uuid,
          appointmentUuid = appointmentUuid,
          userUuid = userUuid,
          type = type,
          result = result,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt)
    }
  }

  fun pendingRecordsCount(): Observable<Int> {
    return dao
        .count(SyncStatus.PENDING)
        .toObservable()
  }

}
