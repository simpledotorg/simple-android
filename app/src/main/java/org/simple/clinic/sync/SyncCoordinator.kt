package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toNullable
import timber.log.Timber
import javax.inject.Inject

class SyncCoordinator @Inject constructor() {

  fun <T : Any, P> push(
      repository: SynceableRepository<T, P>,
      pushNetworkCall: (List<T>) -> DataPushResponse
  ) {
    val pendingSyncRecords = repository.recordsWithSyncStatus(SyncStatus.PENDING)

    if (pendingSyncRecords.isNotEmpty()) {
      val response = pushNetworkCall(pendingSyncRecords)
      repository.setSyncStatus(SyncStatus.PENDING, SyncStatus.DONE)

      val validationErrors = response.validationErrors
      handleValidationErrors(validationErrors, pendingSyncRecords, repository)
    }
  }

  private fun <P, T : Any> handleValidationErrors(
      validationErrors: List<ValidationErrors>,
      pendingSyncRecords: List<T>,
      repository: SynceableRepository<T, P>
  ) {
    val recordIdsWithErrors = validationErrors.map { it.uuid }
    if (recordIdsWithErrors.isNotEmpty()) {
      val recordType = pendingSyncRecords.first().javaClass.simpleName
      Timber.e("Server sent validation errors when syncing $recordType : $validationErrors")

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
