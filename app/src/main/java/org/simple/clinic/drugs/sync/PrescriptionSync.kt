package org.simple.clinic.drugs.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class PrescriptionSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: PrescriptionSyncApi,
    private val repository: PrescriptionRepository,
    private val userSession: UserSession,
    @Named("last_prescription_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override val name: String = "Prescribed Drug"

  override fun sync(): Completable =
      canSyncData()
          .flatMapCompletable { canSync ->
            if (canSync) {
              Completable.mergeArrayDelayError(push(), pull())

            } else {
              Completable.complete()
            }
          }

  override fun push() = syncCoordinator.push(repository) { api.push(toRequest(it)) }

  override fun pull(): Completable {
    return syncConfig()
        .map { it.batchSize.numberOfRecords }
        .flatMapCompletable { batchSize ->
          syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it) }
        }
  }

  override fun syncConfig() = Single.just(config)

  private fun toRequest(drugs: List<PrescribedDrug>): PrescriptionPushRequest {
    val payloads = drugs.map { it.toPayload() }
    return PrescriptionPushRequest(payloads)
  }
}
