package org.simple.clinic.bloodsugar.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class BloodSugarSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: BloodSugarSyncApi,
    private val repository: BloodSugarRepository,
    private val userSession: UserSession,
    @Named("last_blood_sugar_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override val name: String = "Blood Sugar"

  override fun sync(): Completable =
      canSyncData()
          .flatMapCompletable { canSync ->
            if (canSync) {
              Completable.mergeArrayDelayError(push(), pull())

            } else {
              Completable.complete()
            }
          }

  override fun push() = Completable.fromAction { syncCoordinator.push(repository) { api.push(toRequest(it)).execute().body()!! } }

  override fun pull(): Completable {
    return Single
        .fromCallable { config.batchSize }
        .flatMapCompletable { batchSize ->
          syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().body()!! }
        }
  }

  override fun syncConfig(): SyncConfig = config

  private fun toRequest(measurements: List<BloodSugarMeasurement>): BloodSugarPushRequest {
    val payloads = measurements.map { it.toPayload() }
    return BloodSugarPushRequest(payloads)
  }
}
