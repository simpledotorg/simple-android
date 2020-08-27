package org.simple.clinic.help

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class HelpSync @Inject constructor(
    private val syncApi: HelpApi,
    private val syncRepository: HelpRepository,
    @Named("sync_config_daily") private val config: SyncConfig
) : ModelSync {

  override val name: String = "Help"

  override val requiresSyncApprovedUser = false

  override fun sync(): Completable = Completable.mergeArrayDelayError(push(), pull())

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable =
      syncApi
          .help()
          .flatMapCompletable(syncRepository::updateHelp)

  override fun syncConfig(): SyncConfig = config

  fun pullWithResult(): Single<HelpPullResult> {
    return pull()
        .toSingleDefault(HelpPullResult.Success as HelpPullResult)
        .onErrorReturn { cause ->
          when (cause) {
            is IOException -> HelpPullResult.NetworkError
            else -> HelpPullResult.OtherError
          }
        }
  }
}
