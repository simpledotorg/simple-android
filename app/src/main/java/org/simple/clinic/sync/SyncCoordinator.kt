package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import timber.log.Timber
import javax.inject.Inject

class SyncCoordinator @Inject constructor() {

  fun <T : Any, P> push(
      repository: SynceableRepository<T, P>,
      pushNetworkCall: (List<T>) -> Single<DataPushResponse>
  ): Completable = repository
      .recordsWithSyncStatus(SyncStatus.PENDING)
      .filter { it.isNotEmpty() }
      .flatMapSingleElement { pushNetworkCall(it).doOnSuccess(logValidationErrorsIfAny(it)) }
      .map { it.validationErrors }
      .map { errors -> errors.map { it.uuid } }
      .flatMapCompletable { recordIdsWithErrors ->
        repository
            .setSyncStatus(from = SyncStatus.PENDING, to = SyncStatus.DONE)
            .andThen(when {
              recordIdsWithErrors.isEmpty() -> Completable.complete()
              else -> repository.setSyncStatus(recordIdsWithErrors, SyncStatus.INVALID)
            })
      }

  private fun <T : Any> logValidationErrorsIfAny(records: List<T>): Consumer<in DataPushResponse> {
    return Consumer { response ->
      if (response.validationErrors.isNotEmpty()) {
        val recordType = records.first().javaClass.simpleName
        Timber.e("Server sent validation errors when syncing $recordType : ${response.validationErrors}")
      }
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
