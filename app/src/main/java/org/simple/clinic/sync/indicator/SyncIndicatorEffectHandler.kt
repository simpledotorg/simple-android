package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.sync.LastSyncedState

class SyncIndicatorEffectHandler @AssistedInject constructor(
    private val lastSyncedState: Preference<LastSyncedState>,
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
          .build()

  private fun fetchLastSyncedState(): ObservableTransformer<FetchLastSyncedStatus, SyncIndicatorEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap { lastSyncedState.asObservable() }
          .map(::LastSyncedStateFetched)
    }
  }
}
