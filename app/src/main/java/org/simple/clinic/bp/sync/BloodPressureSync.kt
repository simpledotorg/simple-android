package org.simple.clinic.bp.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class BloodPressureSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: BloodPressureSyncApi,
    private val repository: BloodPressureRepository,
    private val userSession: UserSession,
    @Named("last_bp_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override val name: String = "Blood Pressure"

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

  private fun toRequest(measurements: List<BloodPressureMeasurement>): BloodPressurePushRequest {
    val payloads = measurements.map { it.toPayload() }
    return BloodPressurePushRequest(payloads)
  }
}
