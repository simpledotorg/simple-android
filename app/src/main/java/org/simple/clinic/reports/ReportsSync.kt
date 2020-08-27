package org.simple.clinic.reports

import io.reactivex.Completable
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import javax.inject.Inject
import javax.inject.Named

class ReportsSync @Inject constructor(
    private val reportsApi: ReportsApi,
    private val reportsRepository: ReportsRepository,
    @Named("sync_config_frequent") private val config: SyncConfig
) : ModelSync {

  override val name: String = "Reports"

  override val requiresSyncApprovedUser = true

  override fun sync(): Completable = Completable
      .mergeArrayDelayError(
          Completable.fromAction { push() },
          Completable.fromAction { pull() }
      )

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    val reportsText = reportsApi.userAnalytics().execute().body()!!

    reportsRepository.updateReports(reportsText)
  }

  override fun syncConfig(): SyncConfig = config
}
