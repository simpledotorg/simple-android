package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

interface Synceable
interface SynceablePayload<T : Synceable>

interface SynceableRepository<T : Synceable, P: SynceablePayload<T>> {

  fun pendingSyncRecords(): Single<List<T>>

  fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable

  fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable

  fun mergeWithLocalData(payloads: List<P>): Completable
}

// TODO: Use this for syncing all data models.
class DataSync @Inject constructor(val configProvider: Single<SyncConfig>) {

  fun <T : Synceable, P : SynceablePayload<T>> push(
      repository: SynceableRepository<T, P>,
      pushNetworkCall: (List<T>) -> Single<DataPushResponse>
  ): Completable {
    val cachedPendingSyncRecords = repository.pendingSyncRecords()
        .toObservable()
        .filter { it.isNotEmpty() }
        .cache()

    val markAsInFlight = cachedPendingSyncRecords
        .flatMapCompletable {
          repository.setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.IN_FLIGHT)
        }

    val sendRecords = cachedPendingSyncRecords
        .flatMapSingle { pushNetworkCall(it).doOnSuccess(logValidationErrorsIfAny(it)) }
        .map { it.validationErrors }
        .map { errors -> errors.map { it.uuid } }
        .flatMapCompletable { recordIdsWithErrors ->
          repository
              .setSyncStatus(from = SyncStatus.IN_FLIGHT, to = SyncStatus.DONE)
              .andThen(when {
                recordIdsWithErrors.isEmpty() -> Completable.complete()
                else -> repository.setSyncStatus(recordIdsWithErrors, SyncStatus.INVALID)
              })
        }

    return markAsInFlight.andThen(sendRecords)
  }

  private fun <T : Synceable> logValidationErrorsIfAny(records: List<T>): Consumer<in DataPushResponse> {
    return Consumer { response ->
      if (response.validationErrors.isNotEmpty()) {
        val recordType = records.first().javaClass.simpleName
        Timber.e("Server sent validation errors when syncing $recordType : ${response.validationErrors}")
      }
    }
  }

  fun <T : Synceable, P : SynceablePayload<T>> pull(
      repository: SynceableRepository<T, P>,
      lastPullTimestamp: Preference<Optional<Instant>>,
      pullNetworkCall: (recordsToPull: Int, lastPull: Instant?) -> Single<out DataPullResponse<P>>
  ): Completable {
    return configProvider
        .flatMapCompletable { config ->
          lastPullTimestamp.asObservable()
              .take(1)
              .flatMapSingle { (lastPull) -> pullNetworkCall(config.batchSize, lastPull) }
              .flatMap { response ->
                repository.mergeWithLocalData(response.payloads)
                    .andThen(Completable.fromAction { lastPullTimestamp.set(Just(response.processedSinceTimestamp)) })
                    .andThen(Observable.just(response))
              }
              .repeat()
              .takeWhile { response -> response.payloads.size >= config.batchSize }
              .ignoreElements()
        }
  }
}
