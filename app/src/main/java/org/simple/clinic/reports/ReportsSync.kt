package org.simple.clinic.reports

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.user.UserSession
import javax.inject.Inject
import javax.inject.Named

class ReportsSync @Inject constructor(
    private val reportsApi: ReportsApi,
    private val reportsRepository: ReportsRepository,
    private val userSession: UserSession,
    @Named("sync_config_frequent") private val configProvider: Single<SyncConfig>
) : ModelSync {
  private fun canSyncData() = userSession.canSyncData().firstOrError()

  override fun sync(): Completable =
      canSyncData()
          .flatMapCompletable { canSync ->
            if (canSync) {
              Completable.mergeArrayDelayError(push(), pull())

            } else {
              Completable.complete()
            }
          }

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable =
      reportsApi
          .userAnalytics()
          .flatMapCompletable(reportsRepository::updateReports)

  override fun syncConfig() = configProvider
}
