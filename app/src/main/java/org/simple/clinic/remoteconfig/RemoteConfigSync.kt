package org.simple.clinic.remoteconfig

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.sync.BatchSize
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.Unauthorized
import org.simple.clinic.util.ResolvedError.Unexpected
import org.simple.clinic.util.exhaustive
import timber.log.Timber
import javax.inject.Inject

class RemoteConfigSync @Inject constructor(
    private val crashReporter: CrashReporter,
    private val remoteConfigService: RemoteConfigService
) : ModelSync {

  override fun sync(): Completable = pull()

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable {
    return remoteConfigService.update()
        .doOnError(::logError)
        .onErrorComplete()
  }

  private fun logError(error: Throwable) {
    val resolvedError = ErrorResolver.resolve(error)
    when (resolvedError) {
      is Unexpected -> {
        crashReporter.report(resolvedError.actualCause)
        Timber.e(resolvedError.actualCause)
      }
      is NetworkRelated, is Unauthorized -> Timber.e(error)
    }.exhaustive()
  }

  override fun syncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = BatchSize.SMALL,
        syncGroup = SyncGroup.FREQUENT))
  }
}
