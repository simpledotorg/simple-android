package org.simple.clinic.bp.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named

class BloodPressureSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: BloodPressureSyncApi,
    private val repository: BloodPressureRepository,
    @Named("last_bp_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Blood Pressure"

  override val requiresSyncApprovedUser = true

  override fun push() = syncCoordinator.push(repository, config.pushBatchSize) { api.push(toRequest(it)).execute().read()!! }

  override fun pull() {
    val batchSize = config.pullBatchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  private fun toRequest(measurements: List<BloodPressureMeasurement>): BloodPressurePushRequest {
    val payloads = measurements.map { it.toPayload() }
    return BloodPressurePushRequest(payloads)
  }
}
