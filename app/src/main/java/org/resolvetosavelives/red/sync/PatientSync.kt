package org.resolvetosavelives.red.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.newentry.search.PatientWithAddress
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

/**
 * Here lies the greatest algorithm ever written for syncing patients with the server.
 */
class PatientSync @Inject constructor(
    private val api: PatientSyncApiV1,
    private val repository: PatientRepository,
    @Named("first_patient_sync_done") private val firstSyncDone: Preference<Boolean>
) {

  fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  fun push(): Completable {
    val cachedPatients = repository
        .pendingSyncPatients()
        .cache()

    val pushResult = cachedPatients
        // Converting to an Observable because Single#filter() returns a Maybe.
        .toObservable()
        .flatMapIterable { list -> list }
        .map { databaseModel -> databaseModel.toPayload() }
        .toList()
        .filter({ it.isNotEmpty() })
        .map(::PatientPushRequest)
        .flatMapSingle { request -> api.push(request) }
        .flatMap {
          when {
            it.hasValidationErrors() -> Single.just(FailedWithValidationErrors(it.validationErrors))
            else -> cachedPatients.map(::Pushed)
          }
        }

    return pushResult
        .flatMapCompletable { result ->
          when (result) {
            is Pushed -> repository.markPatientsAsSynced(result.syncedPatients)
            is FailedWithValidationErrors -> logValidationErrors(result.errors)
          }
        }
  }

  private fun logValidationErrors(errors: List<ValidationErrors>?): Completable? {
    return { Timber.e("Server sent validation errors for patients: $errors") }.toCompletable()
  }

  fun pull(): Completable {
    // TODO.
    return Completable.complete()
  }
}

sealed class PatientPullResult
data class Pushed(val syncedPatients: List<PatientWithAddress>) : PatientPullResult()
data class FailedWithValidationErrors(val errors: List<ValidationErrors>?) : PatientPullResult()
