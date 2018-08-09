package org.simple.clinic.facility

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class FacilitySync @Inject constructor(
    private val api: FacilitySyncApiV1,
    private val repository: FacilityRepository,
    private val configProvider: Single<SyncConfig>,
    @Named("last_facility_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable {
    return pull()
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
                repository.mergeWithLocalData(response.facilities)
                    .observeOn(Schedulers.single())
                    .andThen({ lastPullTimestamp.set(Just(response.processedSinceTimestamp)) }.toCompletable())
                    .andThen(Observable.just(response))
              }
              .repeat()
              .takeWhile { response -> response.facilities.size >= config.batchSize }
              .ignoreElements()
        }
  }

  fun pullWithResult(): Single<FacilityPullResult> {
    return pull()
        .toSingleDefault(FacilityPullResult.Success() as FacilityPullResult)
        .onErrorReturn { e ->
          when (e) {
            is IOException -> FacilityPullResult.NetworkError()
            else -> FacilityPullResult.UnexpectedError()
          }
        }
  }
}
