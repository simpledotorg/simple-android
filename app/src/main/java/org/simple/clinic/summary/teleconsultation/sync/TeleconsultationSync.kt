package org.simple.clinic.summary.teleconsultation.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastTeleconsultationFacilityPullToken
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class TeleconsultationSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: TeleconsultationFacilityRepository,
    private val api: TeleconsultFacilityInfoApi,
    private val userSession: UserSession,
    @TypedPreference(LastTeleconsultationFacilityPullToken) private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_daily") private val config: SyncConfig
) : ModelSync {

  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override val name: String = "TeleconsultationFacilityInfo"

  override fun sync(): Completable {
    return canSyncData()
        .flatMapCompletable { canSync ->
          if (canSync) {
            Completable.mergeArrayDelayError(push(), pull())
          } else {
            Completable.complete()
          }
        }
  }

  override fun push(): Completable {
    return Completable.complete()
  }

  override fun pull(): Completable {
    return Single
        .fromCallable { config.batchSize }
        .flatMapCompletable { batchSize ->
          syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().body()!! }
        }
  }

  override fun syncConfig() = config
}
