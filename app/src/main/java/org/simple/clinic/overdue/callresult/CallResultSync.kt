package org.simple.clinic.overdue.callresult

import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import javax.inject.Inject

class CallResultSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: CallResultRepository,
    private val api: CallResultSyncApi,
    @SyncConfigType(SyncConfigType.Type.Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "CallResult"

  override val requiresSyncApprovedUser: Boolean = true

  override fun push() {
    syncCoordinator.push(repository, config.pushBatchSize) { callResultsToPush ->
      api.push(toRequest(callResultsToPush)).execute().read()!!
    }
  }

  private fun toRequest(callResults: List<CallResult>): CallResultPushRequest {
    val payloads = callResults.map(::payloadFromCallResult)

    return CallResultPushRequest(payloads)
  }

  private fun payloadFromCallResult(callResult: CallResult) = CallResultPayload(
      id = callResult.id,
      userId = callResult.userId,
      appointmentId = callResult.appointmentId,
      removeReason = callResult.removeReason,
      outcome = callResult.outcome,
      createdAt = callResult.timestamps.createdAt,
      updatedAt = callResult.timestamps.updatedAt,
      deletedAt = callResult.timestamps.deletedAt
  )

  override fun pull() {
    /* Nothing to do here */
  }
}
