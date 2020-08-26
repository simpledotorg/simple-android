package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import timber.log.Timber
import javax.inject.Inject

class SyncCoordinator @Inject constructor() {

  fun <T : Any, P> push(
      repository: SynceableRepository<T, P>,
      pushNetworkCall: (List<T>) -> Single<DataPushResponse>
  ): Completable {
    return Completable
        .fromAction {
          val pendingSyncRecords = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()

          if (pendingSyncRecords.isNotEmpty()) {
            val response = pushNetworkCall(pendingSyncRecords).blockingGet()
            repository.setSyncStatus(SyncStatus.PENDING, SyncStatus.DONE).blockingAwait()

            val validationErrors = response.validationErrors
            val recordIdsWithErrors = validationErrors.map { it.uuid }
            if (recordIdsWithErrors.isNotEmpty()) {
              logValidationErrorsIfAny(pendingSyncRecords, validationErrors)
              repository.setSyncStatus(recordIdsWithErrors, SyncStatus.INVALID)
            }
          }
        }
  }

  private fun <T : Any> logValidationErrorsIfAny(
      records: List<T>,
      validationErrors: List<ValidationErrors>
  ) {
    if (validationErrors.isNotEmpty()) {
      val recordType = records.first().javaClass.simpleName
      Timber.e("Server sent validation errors when syncing $recordType : ${validationErrors}")
    }
  }

  fun <T : Any, P> pull(
      repository: SynceableRepository<T, P>,
      lastPullToken: Preference<Optional<String>>,
      batchSize: Int,
      pullNetworkCall: (String?) -> Single<out DataPullResponse<P>>
  ): Completable {
    return lastPullToken.asObservable()
        .take(1)
        .flatMapSingle { (lastPull) -> pullNetworkCall(lastPull) }
        .flatMap { response ->
          repository.mergeWithLocalData(response.payloads)
              .andThen(Completable.fromAction { lastPullToken.set(Just(response.processToken)) })
              .andThen(Observable.just(response))
        }
        .repeat()
        .takeWhile { response -> response.payloads.size >= batchSize }
        .ignoreElements()
  }
}
