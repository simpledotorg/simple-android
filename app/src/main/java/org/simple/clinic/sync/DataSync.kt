package org.simple.clinic.sync

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.di.AppScope
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.exhaustive
import timber.log.Timber
import javax.inject.Inject

@AppScope
class DataSync @Inject constructor(
    private val modelSyncs: ArrayList<ModelSync>,
    private val crashReporter: CrashReporter
) {

  private val syncResults = PublishSubject.create<Pair<SyncGroup, SyncGroupResult>>()

  fun sync(syncGroup: SyncGroup?): Completable {
    return if (syncGroup == null) {
      val allSyncGroups = SyncGroup.values()
      Completable.merge(allSyncGroups.map { syncGroup(it) })
    } else {
      syncGroup(syncGroup)
    }
  }

  private fun syncGroup(syncGroup: SyncGroup): Completable {
    syncResults.onNext(Pair(syncGroup, SyncGroupResult.SYNCING))

    return Observable
        .fromIterable(modelSyncs)
        .flatMapSingle { modelSync ->
          modelSync
              .syncConfig()
              .map { config -> config to modelSync }
        }
        .filter { (config, _) -> config.syncGroup == syncGroup }
        .map { (_, modelSync) -> modelSync.sync() }
        .toList()
        .flatMapCompletable { runAndSwallowErrors(it, syncGroup) }
  }

  private fun runAndSwallowErrors(completables: List<Completable>, syncGroup: SyncGroup): Completable {
    return Completable
        .mergeDelayError(completables)
        .doOnComplete { syncResults.onNext(Pair(syncGroup, SyncGroupResult.SUCCESS)) }
        .doOnError {
          syncResults.onNext(Pair(syncGroup, SyncGroupResult.FAILURE))
          logError()
        }
        .onErrorComplete()
  }

  private fun logError() = { e: Throwable ->
    val resolvedError = ErrorResolver.resolve(e)
    when (resolvedError) {
      is ResolvedError.Unexpected -> {
        Timber.i("(breadcrumb) Reporting to sentry. Error: $e. Resolved error: $resolvedError")
        crashReporter.report(resolvedError.actualCause)
        Timber.e(resolvedError.actualCause)
      }
      is ResolvedError.NetworkRelated -> {
        // Connectivity issues are expected.
        Timber.e(e)
      }
    }.exhaustive()
  }

  fun streamSyncResults(): Observable<Pair<SyncGroup, SyncGroupResult>> = syncResults
}
