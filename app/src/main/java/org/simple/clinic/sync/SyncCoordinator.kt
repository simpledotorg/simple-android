package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.toNullable
import timber.log.Timber
import java.util.Optional
import javax.inject.Inject

class SyncCoordinator @Inject constructor() {

  fun <T : Any, P> push(
      repository: SynceableRepository<T, P>,
      batchSize: Int,
      pushNetworkCall: (List<T>) -> DataPushResponse
  ) {
    var offset = 0
    var recordsToSync = repository.pendingSyncRecords(
        limit = batchSize,
        offset = 0
    )

    while (recordsToSync.isNotEmpty()) {
      val response = pushNetworkCall(recordsToSync)

      val validationErrors = response.validationErrors
      logValidationErrors(validationErrors, recordsToSync)
      markInvalidSyncRecords(validationErrors, repository)

      offset += recordsToSync.size
      recordsToSync = repository.pendingSyncRecords(
          limit = batchSize,
          offset = offset
      )
    }

    repository.setSyncStatus(SyncStatus.PENDING, SyncStatus.DONE)
  }

  private fun <T : Any> logValidationErrors(
      validationErrors: List<ValidationErrors>,
      pendingSyncRecords: List<T>
  ) {
    if (validationErrors.isNotEmpty()) {
      val recordType = pendingSyncRecords.first().javaClass.simpleName
      Timber.e("Server sent validation errors when syncing $recordType : $validationErrors")
    }
  }

  private fun <T : Any, P> markInvalidSyncRecords(
      validationErrors: List<ValidationErrors>,
      repository: SynceableRepository<T, P>
  ) {
    val recordIdsWithErrors = validationErrors.map { it.uuid }

    if (recordIdsWithErrors.isNotEmpty()) {
      repository.setSyncStatus(recordIdsWithErrors, SyncStatus.INVALID)
    }
  }

  fun <T : Any, P> pull(
      repository: SynceableRepository<T, P>,
      lastPullToken: Preference<Optional<String>>,
      batchSize: Int,
      pullNetworkCall: (String?) -> DataPullResponse<P>
  ) {
    var hasFetchedAllData = false

    while (!hasFetchedAllData) {
      val processToken = lastPullToken.get().toNullable()

      val response = pullNetworkCall(processToken)

      repository.mergeWithLocalData(response.payloads)
      lastPullToken.set(Optional.of(response.processToken))

      hasFetchedAllData = response.payloads.size < batchSize
    }
  }
}
