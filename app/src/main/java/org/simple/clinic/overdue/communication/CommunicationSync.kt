package org.simple.clinic.overdue.communication

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class CommunicationSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: CommunicationRepository,
    private val api: CommunicationSyncApiV2,
    @Named("last_communication_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val configProvider: Single<SyncConfig>,
    val userSession: UserSession
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override fun sync(): Completable =
      canSyncData()
          .flatMapCompletable { canSync ->
            if (canSync) {
              Completable.mergeArrayDelayError(push(), pull())

            } else {
              Completable.complete()
            }
          }

  override fun push(): Completable {
    return syncCoordinator.push(repository, pushNetworkCall = { api.push(toRequest(it)) })
  }

  override fun pull(): Completable {
    return configProvider
        .map { it.batchSize }
        .flatMapCompletable { batchSize ->
          syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it) }
        }
  }

  override fun syncInterval(): Single<SyncInterval> {
    return configProvider.map { it.syncInterval }
  }

  private fun toRequest(schedules: List<Communication>): CommunicationPushRequest {
    val payloads = schedules
        .map {
          it.run {
            CommunicationPayload(
                uuid = uuid,
                appointmentUuid = appointmentUuid,
                userUuid = userUuid,
                type = type,
                result = result,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt)
          }
        }
        .toList()
    return CommunicationPushRequest(payloads)
  }
}
