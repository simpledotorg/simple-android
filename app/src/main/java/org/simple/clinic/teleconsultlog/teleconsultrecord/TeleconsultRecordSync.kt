package org.simple.clinic.teleconsultlog.teleconsultrecord

import io.reactivex.Completable
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import javax.inject.Inject
import javax.inject.Named

class TeleconsultRecordSync @Inject constructor(
    private val teleconsultRecordApi: TeleconsultRecordApi,
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val syncCoordinator: SyncCoordinator,
    @Named("sync_config_daily") private val config: SyncConfig
) : ModelSync {
  
  override val name: String = "TeleconsultRecord"

  override val requiresSyncApprovedUser: Boolean = true

  override fun sync(): Completable {
    return Completable.fromAction { push() } // Api endpoint is push only
  }

  override fun push() {
    syncCoordinator.push(
        teleconsultRecordRepository
    ) { teleconsultRecordApi.push(toRequest(it)).execute().read()!! }
  }

  override fun pull() {
    /* Nothing to do here */
  }

  override fun syncConfig(): SyncConfig = config

  private fun toRequest(teleconsultRecord: List<TeleconsultRecord>): TeleconsultPushRequest {
    val payloads = teleconsultRecord.map { it.toPayload() }
    return TeleconsultPushRequest(payloads)
  }
}
