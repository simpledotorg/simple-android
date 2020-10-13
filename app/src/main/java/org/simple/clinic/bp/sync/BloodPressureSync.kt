package org.simple.clinic.bp.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.sync.SyncTag
import org.simple.clinic.util.Optional
import org.simple.clinic.util.read
import javax.inject.Inject
import javax.inject.Named

class BloodPressureSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: BloodPressureSyncApi,
    private val repository: BloodPressureRepository,
    @Named("last_bp_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  override val name: String = "Blood Pressure"

  override val requiresSyncApprovedUser = true

  override fun sync(): Completable = Completable
      .mergeArrayDelayError(
          Completable.fromAction { push() },
          Completable.fromAction { pull() }
      )

  override fun push() = syncCoordinator.push(repository) { api.push(toRequest(it)).execute().read()!! }

  override fun pull() {
    val batchSize = config.batchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  override fun syncConfig(): SyncConfig = config

  override fun syncTags() = setOf(SyncTag.FREQUENT)

  private fun toRequest(measurements: List<BloodPressureMeasurement>): BloodPressurePushRequest {
    val payloads = measurements.map { it.toPayload() }
    return BloodPressurePushRequest(payloads)
  }
}
