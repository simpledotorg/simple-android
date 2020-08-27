package org.simple.clinic.sync

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.di.AppScope
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.SyncAnalyticsEvent
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.ServerError
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.ResolvedError.Unexpected
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toOptional
import timber.log.Timber
import javax.inject.Inject

@AppScope
class DataSync @Inject constructor(
    private val modelSyncs: ArrayList<ModelSync>,
    private val crashReporter: CrashReporter,
    private val schedulersProvider: SchedulersProvider,
    private val userSession: UserSession
) {

  private val syncProgress = PublishSubject.create<SyncGroupResult>()

  private val syncErrors = PublishSubject.create<ResolvedError>()

  fun syncTheWorld(): Completable {
    val syncAllGroups = SyncGroup
        .values()
        .map(this::sync)

    return Completable.merge(syncAllGroups)
  }

  fun sync(syncGroup: SyncGroup): Completable {
    val syncsInGroup = modelSyncs.filter { it.syncConfig().syncGroup == syncGroup }

    return Single
        .fromCallable { userSession.loggedInUserImmediate().toOptional() }
        .flatMapObservable { user ->
          syncsInGroup
              .toObservable()
              .map { user to it }
        }
        .filter { (user, modelSync) ->
          if (modelSync.requiresSyncApprovedUser) {
            user.isPresent() && user.get().canSyncData
          } else true
        }
        .map { (_, modelSync) -> modelSync }
        .toList()
        .map(::modelSyncsToCompletables)
        .flatMapCompletable { runAndSwallowErrors(it, syncGroup) }
        .doOnSubscribe { syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.SYNCING)) }
  }

  private fun modelSyncsToCompletables(modelSyncs: List<ModelSync>): List<Completable> {
    val allPushes = modelSyncs.map { Completable.fromAction(it::push) }
    val allPulls = modelSyncs.map { Completable.fromAction(it::pull) }

    return allPushes + allPulls
  }

  // TODO (vs) 27/08/20: Report sync events in a later commit
  private fun reportSyncEvent(name: String, event: SyncAnalyticsEvent) {
    Timber.tag("Sync").i("Started sync: $name")
    Analytics.reportSyncEvent(name, event)
  }

  fun fireAndForgetSync(syncGroup: SyncGroup) {
    sync(syncGroup)
        .subscribeOn(schedulersProvider.io())
        .subscribe()
  }

  fun fireAndForgetSync() {
    syncTheWorld()
        .subscribeOn(schedulersProvider.io())
        .subscribe()
  }

  private fun runAndSwallowErrors(completables: List<Completable>, syncGroup: SyncGroup): Completable {
    return Completable
        .mergeDelayError(completables)
        .doOnComplete { syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.SUCCESS)) }
        .doOnError { syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.FAILURE)) }
        .doOnError(::logError)
        .onErrorComplete()
  }

  private fun logError(cause: Throwable) {
    val resolvedError = ErrorResolver.resolve(cause)
    syncErrors.onNext(resolvedError)

    when (resolvedError) {
      is Unexpected, is ServerError -> {
        Timber.i("(breadcrumb) Reporting to sentry. Error: $cause. Resolved error: $resolvedError")
        crashReporter.report(resolvedError.actualCause)
        Timber.e(resolvedError.actualCause)
      }
      is NetworkRelated, is Unauthenticated -> Timber.e(cause)
    }.exhaustive()
  }

  fun streamSyncResults(): Observable<SyncGroupResult> = syncProgress

  fun streamSyncErrors(): Observable<ResolvedError> = syncErrors

  data class SyncGroupResult(val syncGroup: SyncGroup, val syncProgress: SyncProgress)

}
