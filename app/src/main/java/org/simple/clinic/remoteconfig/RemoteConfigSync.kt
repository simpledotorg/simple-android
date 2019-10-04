package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
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
    private val remoteConfig: FirebaseRemoteConfig,
    private val cacheExpiration: FirebaseRemoteConfigCacheExpiration,
    private val crashReporter: CrashReporter,
    private val configReader: ConfigReader
) : ModelSync {

  override fun sync(): Completable = pull()

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable {
    val fetch = Completable.fromAction {
      remoteConfig.fetch(cacheExpiration.value.seconds)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              Timber.i("Firebase remote config updated successfully")
              remoteConfig.activateFetched()
            } else {
              Timber.w("Failed to update Firebase remote config")
            }
          }
    }

    return fetch
        .doOnError(logError())
        .onErrorComplete()
  }

  private fun logError() = { e: Throwable ->
    if (e is FirebaseRemoteConfigFetchThrottledException) {
      // This is expected.
      crashReporter.report(e)

    } else {
      val resolvedError = ErrorResolver.resolve(e)
      when (resolvedError) {
        is Unexpected -> {
          crashReporter.report(resolvedError.actualCause)
          Timber.e(resolvedError.actualCause)
        }
        is NetworkRelated, is Unauthorized -> {
          Timber.e(e)
        }
      }.exhaustive()
    }
  }

  override fun syncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = BatchSize.SMALL,
        syncGroup = SyncGroup.FREQUENT))
  }
}
