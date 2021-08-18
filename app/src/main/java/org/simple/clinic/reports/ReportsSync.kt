package org.simple.clinic.reports

import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.util.read
import javax.inject.Inject

class ReportsSync @Inject constructor(
    private val reportsApi: ReportsApi,
    private val reportsRepository: ReportsRepository,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Reports"

  override val requiresSyncApprovedUser = true

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    val reportsText = reportsApi.userAnalytics().execute().read()!!

    reportsRepository.updateReports(reportsText)
  }

}
