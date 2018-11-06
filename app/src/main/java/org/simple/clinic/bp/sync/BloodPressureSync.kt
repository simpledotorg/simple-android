package org.simple.clinic.bp.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class BloodPressureSync @Inject constructor(
    private val api: BloodPressureSyncApiV1,
    private val repository: BloodPressureRepository,
    private val configProvider: Single<SyncConfig>,
    @Named("last_bp_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  fun push(): Completable {
    val recoverStuckRecordsFromPreviousSync = repository.updateMeasurementsSyncStatus(SyncStatus.IN_FLIGHT, SyncStatus.PENDING)

    val cachedPendingSyncMeasurements = repository.measurementsWithSyncStatus(SyncStatus.PENDING)
        // Converting to an Observable because Single#filter() returns a Maybe.
        // And Maybe#flatMapSingle() throws a NoSuchElementException on completion.
        .toObservable()
        .filter { it.isNotEmpty() }
        .cache()

    val pendingToInFlight = cachedPendingSyncMeasurements
        .flatMapCompletable {
          repository.updateMeasurementsSyncStatus(oldStatus = SyncStatus.PENDING, newStatus = SyncStatus.IN_FLIGHT)
        }

    val networkCall = cachedPendingSyncMeasurements
        .map { patients -> patients.map { it.toPayload() } }
        .map(::BloodPressurePushRequest)
        .flatMapSingle { request -> api.push(request) }
        .doOnNext { response -> logValidationErrorsIfAny(response) }
        .map { it.validationErrors }
        .map { errors -> errors.map { it.uuid } }
        .flatMapCompletable { patientUuidsWithErrors ->
          repository
              .updateMeasurementsSyncStatus(oldStatus = SyncStatus.IN_FLIGHT, newStatus = SyncStatus.DONE)
              .andThen(patientUuidsWithErrors.let {
                when {
                  it.isEmpty() -> Completable.complete()
                  else -> repository.updateMeasurementsSyncStatus(patientUuidsWithErrors, SyncStatus.INVALID)
                }
              })
        }

    return recoverStuckRecordsFromPreviousSync
        .andThen(pendingToInFlight)
        .andThen(networkCall)
  }

  private fun logValidationErrorsIfAny(response: DataPushResponse) {
    if (response.validationErrors.isNotEmpty()) {
      Timber.e("Server sent validation errors for BP measurements: ${response.validationErrors}")
    }
  }

  fun pull(): Completable {
    return configProvider
        .flatMapCompletable { config ->
          lastPullTimestamp.asObservable()
              .take(1)
              .flatMapSingle { lastPullTime ->
                when (lastPullTime) {
                  is Just -> api.pull(recordsToPull = config.batchSize, lastPullTimestamp = lastPullTime.value)
                  is None -> api.pull(recordsToPull = config.batchSize)
                }
              }
              .flatMap { response ->
                repository.mergeWithLocalData(response.measurements)
                    .observeOn(Schedulers.single())
                    .andThen({ lastPullTimestamp.set(Just(response.processedSinceTimestamp)) }.toCompletable())
                    .andThen(Observable.just(response))
              }
              .repeat()
              .takeWhile { response -> response.measurements.size >= config.batchSize }
              .ignoreElements()
        }
  }
}
