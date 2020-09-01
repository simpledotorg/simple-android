package org.simple.clinic.protocol.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.protocol.ProtocolSyncApi
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import org.simple.clinic.util.read
import javax.inject.Inject
import javax.inject.Named

class ProtocolSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: ProtocolRepository,
    private val api: ProtocolSyncApi,
    @Named("last_protocol_pull_token") private val lastPullToken: Preference<Optional<String>>,
    @Named("sync_config_daily") private val config: SyncConfig
) : ModelSync {

  override val name: String = "Protocol"

  override val requiresSyncApprovedUser = false

  override fun sync(): Completable = Completable.fromAction { pull() }

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    val batchSize = config.batchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  override fun syncConfig(): SyncConfig = config
}
