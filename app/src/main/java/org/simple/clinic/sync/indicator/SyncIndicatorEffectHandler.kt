package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.Instant

class SyncIndicatorEffectHandler @AssistedInject constructor(
    private val lastSyncedState: Preference<LastSyncedState>,
    private val utcClock: UtcClock,
    //TODO: Fetch the `SyncIndicatorConfig` instead of a stream
    private val syncIndicatorConfig: Observable<SyncIndicatorConfig>,
    private val schedulersProvider: SchedulersProvider,
    private val dataSync: DataSync,
    @Assisted private val uiActions: SyncIndicatorUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: SyncIndicatorUiActions): SyncIndicatorEffectHandler
  }

  fun build(): ObservableTransformer<SyncIndicatorEffect, SyncIndicatorEvent> =
      RxMobius
          .subtypeEffectHandler<SyncIndicatorEffect, SyncIndicatorEvent>()
          .addTransformer(FetchLastSyncedStatus::class.java, fetchLastSyncedState())
          .addTransformer(FetchDataForSyncIndicatorState::class.java, fetchDataForSyncIndicatorState())
          .addTransformer(StartSyncedStateTimer::class.java, startTimer())
          .addTransformer(InitiateDataSync::class.java, startDataSync())
          .build()

  private fun startTimer(): ObservableTransformer<StartSyncedStateTimer, SyncIndicatorEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap { Observable.interval(it.intervalAmount, it.timeUnit) }
          .map(::IncrementTimerTick)
    }
  }

  private fun fetchLastSyncedState(): ObservableTransformer<FetchLastSyncedStatus, SyncIndicatorEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap { lastSyncedState.asObservable() }
          .map(::LastSyncedStateFetched)
    }
  }

  private fun fetchDataForSyncIndicatorState(): ObservableTransformer<FetchDataForSyncIndicatorState, SyncIndicatorEvent> {
    return ObservableTransformer { effect ->
      effect
          .flatMap {
            syncIndicatorConfig.map {
              DataForSyncIndicatorStateFetched(Instant.now(utcClock), it.syncFailureThreshold)
            }
          }
    }
  }

  private fun startDataSync(): ObservableTransformer<InitiateDataSync, SyncIndicatorEvent> {
    return ObservableTransformer { effect ->
      effect
          .doOnNext { dataSync.fireAndForgetSync(SyncGroup.FREQUENT) }
          .switchMap { dataSync.streamSyncErrors() }
          .map(::DataSyncErrorReceived)
    }
  }
}
