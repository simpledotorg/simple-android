package org.simple.clinic.bloodsugar.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named

class BloodSugarSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: BloodSugarSyncApi,
    private val repository: BloodSugarRepository,
    @Named("last_blood_sugar_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Blood Sugar"

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

  private fun toRequest(measurements: List<BloodSugarMeasurement>): BloodSugarPushRequest {
    val payloads = measurements.map { it.toPayload() }
    return BloodSugarPushRequest(payloads)
  }
}
