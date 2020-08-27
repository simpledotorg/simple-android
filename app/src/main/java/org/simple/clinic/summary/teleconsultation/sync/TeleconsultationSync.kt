package org.simple.clinic.summary.teleconsultation.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastTeleconsultationFacilityPullToken
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class TeleconsultationSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: TeleconsultationFacilityRepository,
    private val api: TeleconsultFacilityInfoApi,
    @TypedPreference(LastTeleconsultationFacilityPullToken) private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_daily") private val config: SyncConfig
) : ModelSync {

  override val name: String = "TeleconsultationFacilityInfo"

  override val requiresSyncApprovedUser = true

  override fun sync(): Completable {
    return Completable
        .mergeArrayDelayError(
            Completable.fromAction { push() },
            pull()
        )
  }

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull(): Completable {
    return Completable.fromAction {
      val batchSize = config.batchSize
      syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().body()!! }
    }
  }

  override fun syncConfig() = config
}
