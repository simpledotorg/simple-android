package org.simple.clinic.sync

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.exhaustive
import timber.log.Timber
import javax.inject.Inject

class DataSync @Inject constructor(
    private val modelSyncs: ArrayList<ModelSync>,
    private val crashReporter: CrashReporter
) {

  fun sync(): Completable {
    return runAndDelayError(modelSyncs.map { it.sync() })
  }

  fun syncGroup(syncGroupId: String): Completable {
    return Observable
        .fromIterable(modelSyncs)
        .flatMapSingle { modelSync -> modelSync.syncConfig().zipWith(Single.just(modelSync)) }
        .filter { (config, _) -> config.syncGroupId == syncGroupId }
        .map { (_, modelSync) -> modelSync.sync() }
        .toList()
        .flatMapCompletable(this::runAndDelayError)
  }

  private fun runAndDelayError(completables: List<Completable>): Completable {
    return Completable
        .mergeDelayError(completables)
        .doOnError(logError())
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
}
