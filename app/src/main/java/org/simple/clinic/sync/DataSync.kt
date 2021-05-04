package org.simple.clinic.sync

import androidx.annotation.WorkerThread
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.di.AppScope
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.RemoteConfigService
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.Optional
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.ServerError
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.ResolvedError.Unexpected
import org.simple.clinic.util.ThreadPools
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toOptional
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

private fun createScheduler(workers: Int): Scheduler {
  val executor = ThreadPools.create(workers, workers, "sync-thread")

  return Schedulers.from(executor)
}

@AppScope
class DataSync(
    private val modelSyncs: List<ModelSync>,
    private val crashReporter: CrashReporter,
    private val userSession: UserSession,
    private val schedulersProvider: SchedulersProvider,
    private val syncScheduler: Scheduler,
    private val purgeOnSync: PurgeOnSync
) {

  @Inject
  constructor(
      modelSyncs: List<@JvmSuppressWildcards ModelSync>,
      crashReporter: CrashReporter,
      userSession: UserSession,
      schedulersProvider: SchedulersProvider,
      remoteConfigService: RemoteConfigService,
      purgeOnSync: PurgeOnSync
  ) : this(
      modelSyncs = modelSyncs,
      crashReporter = crashReporter,
      userSession = userSession,
      schedulersProvider = schedulersProvider,
      syncScheduler = createScheduler(remoteConfigService.reader().long("max_parallel_syncs", 1L).toInt()),
      purgeOnSync = purgeOnSync
  )

  private val syncProgress = PublishSubject.create<SyncGroupResult>()

  private val syncErrors = PublishSubject.create<ResolvedError>()

  private fun allSyncs(): Single<List<SyncResult>> {
    val syncAllGroups = SyncGroup
        .values()
        .map(::syncsForGroup)

    return Single
        .merge(syncAllGroups)
        .reduce(listOf(), { list, results -> list + results })
  }

  private fun syncsForGroup(syncGroup: SyncGroup): Single<List<SyncResult>> {
    val syncsInGroup = modelSyncs.filter { it.syncConfig().syncGroup == syncGroup }

    return Single
        .fromCallable { userSession.loggedInUserImmediate().toOptional() }
        .subscribeOn(schedulersProvider.io())
        .compose(filterSyncsThatRequireAuthentication(syncsInGroup))
        .compose(prepareTasksFromSyncs())
        .doOnSubscribe { syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.SYNCING)) }
        .doOnSuccess { syncResults -> syncCompleted(syncResults, syncGroup) }
  }

  private fun filterSyncsThatRequireAuthentication(
      syncsInGroup: List<ModelSync>
  ): SingleTransformer<Optional<User>, List<ModelSync>> {
    return SingleTransformer { userSingle ->
      userSingle
          .flatMap { user ->
            combineSyncsWithCurrentUser(syncsInGroup, user)
                .filter { (user, modelSync) -> shouldSyncBeRun(modelSync, user) }
                .map { (_, modelSync) -> modelSync }
                .toList()
          }
    }
  }

  private fun prepareTasksFromSyncs(): SingleTransformer<List<ModelSync>, List<SyncResult>> {
    return SingleTransformer { modelSyncs ->
      modelSyncs
          .map(::modelSyncsToTasks)
          .flatMapObservable { Observable.fromIterable(it) }
          .flatMapSingle { runAndReportErrors(it).subscribeOn(syncScheduler) }
          .toList()
    }
  }

  private fun combineSyncsWithCurrentUser(
      syncsInGroup: List<ModelSync>,
      user: Optional<User>
  ): Observable<Pair<Optional<User>, ModelSync>> {
    return syncsInGroup
        .toObservable()
        .map { user to it }
  }

  private fun shouldSyncBeRun(
      modelSync: ModelSync,
      user: Optional<User>
  ): Boolean {
    return if (modelSync.requiresSyncApprovedUser) {
      user.isPresent() && user.get().canSyncData
    } else true
  }

  private fun syncCompleted(
      syncResults: List<SyncResult>,
      syncGroup: SyncGroup
  ) {
    val firstFailure = syncResults.firstOrNull { it is SyncResult.Failed }

    if (firstFailure != null) {
      syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.FAILURE))

      val resolvedError = (firstFailure as SyncResult.Failed).error
      syncErrors.onNext(resolvedError)
    } else {
      syncProgress.onNext(SyncGroupResult(syncGroup, SyncProgress.SUCCESS))
    }
  }

  private fun modelSyncsToTasks(modelSyncs: List<ModelSync>): List<Single<SyncResult>> {
    val allPushes = modelSyncs.map(::generatePushOperationForSync)
    val allPulls = modelSyncs.map(::generatePullOperationForSync)

    return allPushes + allPulls
  }

  private fun generatePullOperationForSync(sync: ModelSync): Single<SyncResult> {
    return Completable
        .fromAction(sync::pull)
        .toSingleDefault<SyncResult>(SyncResult.Completed(sync))
        .onErrorReturn { cause -> SyncResult.Failed(sync, ErrorResolver.resolve(cause)) }
  }

  private fun generatePushOperationForSync(sync: ModelSync): Single<SyncResult> {
    return Completable
        .fromAction(sync::push)
        .toSingleDefault<SyncResult>(SyncResult.Completed(sync))
        .onErrorReturn { cause -> SyncResult.Failed(sync, ErrorResolver.resolve(cause)) }
  }

  private fun runAndReportErrors(task: Single<SyncResult>): Single<SyncResult> {
    return task.doOnSuccess { result ->
      if (result is SyncResult.Failed) {
        logError(result.error)
      }
    }
  }

  private fun logError(resolvedError: ResolvedError) {
    val actualCause = resolvedError.actualCause

    when (resolvedError) {
      is Unexpected, is ServerError -> {
        Timber.i("(breadcrumb) Reporting to sentry. Error: ${actualCause}. Resolved error: $resolvedError")
        crashReporter.report(actualCause)
        Timber.e(actualCause)
      }
      is NetworkRelated, is Unauthenticated -> Timber.e(actualCause)
    }.exhaustive()
  }

  private fun purgeOnCompletedSyncs(): SingleTransformer<List<SyncResult>, List<SyncResult>> {
    return SingleTransformer { syncResultsStream ->
      syncResultsStream
          .doOnSuccess { syncResults ->
            val shouldDeleteUnnecessaryRecords = syncResults.haveAllCompleted() && userSession.isUserPresentLocally()

            if (shouldDeleteUnnecessaryRecords) purgeOnSync.purgeUnusedData()
          }
    }
  }

  @WorkerThread
  @Throws(IOException::class) // This is only needed so Mockito can generate mocks for this method correctly
  fun syncTheWorld() {
    allSyncs()
        .compose(purgeOnCompletedSyncs())
        .ignoreElement()
        .blockingAwait()
  }

  @WorkerThread
  fun sync(syncGroup: SyncGroup) {
    syncsForGroup(syncGroup).ignoreElement().blockingAwait()
  }

  fun fireAndForgetSync() {
    allSyncs()
        .compose(purgeOnCompletedSyncs())
        .subscribe()
  }

  fun fireAndForgetSync(syncGroup: SyncGroup) {
    syncsForGroup(syncGroup).subscribe()
  }

  fun streamSyncResults(): Observable<SyncGroupResult> = syncProgress

  fun streamSyncErrors(): Observable<ResolvedError> = syncErrors

  data class SyncGroupResult(val syncGroup: SyncGroup, val syncProgress: SyncProgress)

  private sealed class SyncResult(val sync: ModelSync) {

    data class Completed(
        private val _sync: ModelSync
    ) : SyncResult(_sync)

    data class Failed(
        private val _sync: ModelSync,
        val error: ResolvedError
    ) : SyncResult(_sync)
  }

  private fun Iterable<SyncResult>.haveAllCompleted(): Boolean {
    return all { it is SyncResult.Completed }
  }
}
