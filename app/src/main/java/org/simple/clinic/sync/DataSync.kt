package org.simple.clinic.sync

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.di.AppScope
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.SyncAnalyticsEvent
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.RemoteConfigService
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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

private fun createScheduler(workers: Int): Scheduler {
  val executor = ThreadPoolExecutor(
      workers, // number of threads to keep alive always
      workers, // max number of threads in the pool
      1L, // time to keep non-core threads alive before shutting down
      TimeUnit.SECONDS,
      LinkedBlockingQueue(), // Queue for receiving work
      SyncThreadFactory()
  )

  return Schedulers.from(executor)
}

private class SyncThreadFactory: ThreadFactory {

  private val threadCounter = AtomicInteger(1)

  override fun newThread(job: Runnable): Thread {
    return Thread(job, "sync-thread-${threadCounter.getAndIncrement()}")
  }
}

@AppScope
class DataSync(
    private val modelSyncs: ArrayList<ModelSync>,
    private val crashReporter: CrashReporter,
    private val userSession: UserSession,
    private val schedulersProvider: SchedulersProvider,
    private val syncScheduler: Scheduler
) {

  @Inject constructor(
      modelSyncs: ArrayList<ModelSync>,
      crashReporter: CrashReporter,
      userSession: UserSession,
      schedulersProvider: SchedulersProvider,
      remoteConfigService: RemoteConfigService
  ): this(
      modelSyncs = modelSyncs,
      crashReporter = crashReporter,
      userSession = userSession,
      schedulersProvider = schedulersProvider,
      syncScheduler = createScheduler(remoteConfigService.reader().long("max_parallel_syncs", 1L).toInt())
  )

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
        .subscribeOn(schedulersProvider.io())
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
        .flatMapObservable { Observable.fromIterable(it) }
        .flatMapCompletable { runAndSwallowErrors(it, syncGroup).subscribeOn(syncScheduler) }
        .doOnSubscribe { syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.SYNCING)) }
        .doOnComplete { syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.SUCCESS)) }
        .doOnError { syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.FAILURE)) }
  }

  private fun modelSyncsToCompletables(modelSyncs: List<ModelSync>): List<Completable> {
    val allPushes = modelSyncs.map { sync ->
      Completable
          .fromAction(sync::push)
          .doOnSubscribe { reportSyncEvent(sync.name, "Push", SyncAnalyticsEvent.Started) }
          .doOnComplete { reportSyncEvent(sync.name, "Push", SyncAnalyticsEvent.Completed) }
          .doOnError { reportSyncEvent(sync.name, "Push", SyncAnalyticsEvent.Failed) }
    }
    val allPulls = modelSyncs.map { sync ->
      Completable
          .fromAction(sync::pull)
          .doOnSubscribe { reportSyncEvent(sync.name, "Pull", SyncAnalyticsEvent.Started) }
          .doOnComplete { reportSyncEvent(sync.name, "Pull", SyncAnalyticsEvent.Completed) }
          .doOnError { reportSyncEvent(sync.name, "Pull", SyncAnalyticsEvent.Failed) }
    }

    return allPushes + allPulls
  }

  private fun reportSyncEvent(name: String, type: String, event: SyncAnalyticsEvent) {
    val analyticsName = "$type:$name" // Ex: "Push:Patients"
    Timber.tag("Sync").i("$analyticsName:${event.name}") // Ex: "Push:Patients:Started"
    Analytics.reportSyncEvent(analyticsName, event)
  }

  fun fireAndForgetSync(syncGroup: SyncGroup) {
    sync(syncGroup).subscribe()
  }

  fun fireAndForgetSync() {
    syncTheWorld().subscribe()
  }

  private fun runAndSwallowErrors(completable: Completable, syncGroup: SyncGroup): Completable {
    return completable
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
