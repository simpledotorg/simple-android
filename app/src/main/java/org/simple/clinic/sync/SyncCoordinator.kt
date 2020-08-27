package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toNullable
import timber.log.Timber
import javax.inject.Inject

class SyncCoordinator @Inject constructor() {

  fun <T : Any, P> push(
      repository: SynceableRepository<T, P>,
      pushNetworkCall: (List<T>) -> DataPushResponse
  ): Completable {
    return Completable
        .fromAction {
          val pendingSyncRecords = repository.recordsWithSyncStatus(SyncStatus.PENDING)

          if (pendingSyncRecords.isNotEmpty()) {
            val response = pushNetworkCall(pendingSyncRecords)
            repository.setSyncStatus(SyncStatus.PENDING, SyncStatus.DONE)

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
      pullNetworkCall: (String?) -> DataPullResponse<P>
  ): Completable {
    return Completable.fromAction {

      var hasFetchedAllData = false

      while (!hasFetchedAllData) {
        val processToken = lastPullToken.get().toNullable()

        val response = pullNetworkCall(processToken)

        repository.mergeWithLocalData(response.payloads).blockingAwait()
        lastPullToken.set(Optional.of(response.processToken))

        hasFetchedAllData = response.payloads.size < batchSize
      }
    }
  }
}
