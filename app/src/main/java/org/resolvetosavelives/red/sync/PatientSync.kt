package org.resolvetosavelives.red.sync

import com.f2prateek.rx.preferences2.Preference
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers.single
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.newentry.search.PatientWithAddress
import org.threeten.bp.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

/**
 * Here lies the greatest algorithm ever written for syncing patients with the server.
 */
class PatientSync @Inject constructor(
    private val api: PatientSyncApiV1,
    private val repository: PatientRepository,
    private val configProvider: Single<PatientSyncConfig>,
    @Named("last_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
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
    // Plan:
    // [x] get config values for latest timestamp, batch size etc.
    // [x] make API call in a loop until number of patients received is less than batch size
    // [x] save patients to database
    // [ ] ignore stale patients
    // [x] save last sync time
    return configProvider
        .flatMapCompletable { config ->
          lastPullTimestamp.asObservable()
              .take(1)
              .flatMapSingle { lastPullTime ->
                when (lastPullTime) {
                  is Some -> api.pull(recordsToRetrieve = config.batchSize, latestRecordTimestamp = lastPullTime.value)
                  is None -> api.pull(recordsToRetrieve = config.batchSize, isFirstSync = true)
                }
              }
              .flatMap { response ->
                repository.mergeWithLocalDatabase(response.patients)
                    .observeOn(single())
                    .andThen({ lastPullTimestamp.set(Some(response.latestRecordTimestamp)) }.toCompletable())
                    .andThen(Observable.just(response))
              }
              .repeat()
              .takeWhile({ response -> response.patients.size >= config.batchSize })
              .ignoreElements()
        }
  }
}

sealed class PatientPullResult

data class Pushed(val syncedPatients: List<PatientWithAddress>) : PatientPullResult()

data class FailedWithValidationErrors(val errors: List<ValidationErrors>?) : PatientPullResult()
