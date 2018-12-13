package org.simple.clinic.protocolv2.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.protocolv2.ProtocolRepository
import org.simple.clinic.protocolv2.ProtocolSyncApiV2
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class ProtocolSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: ProtocolRepository,
    private val syncApi: ProtocolSyncApiV2,
    @Named("last_protocol_pull_token") private val lastPullToken: Preference<Optional<String>>
) : ModelSync {

  override fun sync(): Completable = pull()

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable {
    return syncCoordinator.pull(
        repository = repository,
        lastPullToken = lastPullToken,
        pullNetworkCall = syncApi::pull
    )
  }
}
