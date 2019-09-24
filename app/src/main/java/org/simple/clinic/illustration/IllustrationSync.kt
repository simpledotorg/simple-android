package org.simple.clinic.illustration

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import javax.inject.Inject
import javax.inject.Named

class IllustrationSync @Inject constructor(
    private val illustrations: List<HomescreenIllustration>,
    @Named("sync_config_daily") private val configProvider: Single<SyncConfig>
) : ModelSync {

  override fun sync(): Completable = Completable.mergeArrayDelayError(push(), pull())

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable = Completable.complete()

  override fun syncConfig() = configProvider
}
