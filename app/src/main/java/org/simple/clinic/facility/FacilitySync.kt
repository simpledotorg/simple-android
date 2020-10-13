package org.simple.clinic.facility

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.sync.SyncTag
import org.simple.clinic.util.Optional
import org.simple.clinic.util.read
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

  override val requiresSyncApprovedUser = false

  override fun sync() = Completable
      .mergeArrayDelayError(
          Completable.fromAction { push() },
          Completable.fromAction { pull() }
      )

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    val batchSize = config.batchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  override fun syncConfig(): SyncConfig = config

  override fun syncTags() = setOf(SyncTag.DAILY)

  fun pullWithResult(): FacilityPullResult {
    return try {
      pull()
      FacilityPullResult.Success
    } catch (e: Exception) {
      when (e) {
        is IOException -> FacilityPullResult.NetworkError
        else -> FacilityPullResult.UnexpectedError
      }
    }
  }
}
