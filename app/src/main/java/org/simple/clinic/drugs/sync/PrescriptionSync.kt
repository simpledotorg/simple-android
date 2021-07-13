package org.simple.clinic.drugs.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import java.util.Optional
import org.simple.clinic.util.read
import javax.inject.Inject
import javax.inject.Named

class PrescriptionSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: PrescriptionSyncApi,
    private val repository: PrescriptionRepository,
    @Named("last_prescription_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  override val name: String = "Prescribed Drug"

  override val requiresSyncApprovedUser = true

  override fun sync(): Completable = Completable
      .mergeArrayDelayError(
          Completable.fromAction { push() },
          Completable.fromAction { pull() }
      )

  override fun push() {
    syncCoordinator.push(repository, config.pushBatchSize) { api.push(toRequest(it)).execute().read()!! }
  }

  override fun pull() {
    val batchSize = config.pullBatchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  override fun syncConfig(): SyncConfig = config

  private fun toRequest(drugs: List<PrescribedDrug>): PrescriptionPushRequest {
    val payloads = drugs.map { it.toPayload() }
    return PrescriptionPushRequest(payloads)
  }
}
