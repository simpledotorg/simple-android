package org.simple.clinic.facility

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class FacilitySync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: FacilityRepository,
    private val api: FacilitySyncApi,
    @Named("last_facility_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_daily") private val config: SyncConfig
) : ModelSync {

  override val name: String = "Facility"

  override fun sync() = pull()

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable {
    return Completable.fromAction {
      val batchSize = config.batchSize
      syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().body()!! }
    }
  }

  override fun syncConfig(): SyncConfig = config

  fun pullWithResult(): Single<FacilityPullResult> {
    return pull()
        .toSingleDefault(FacilityPullResult.Success as FacilityPullResult)
        .onErrorReturn { e ->
          when (e) {
            is IOException -> FacilityPullResult.NetworkError
            else -> FacilityPullResult.UnexpectedError
          }
        }
  }
}
