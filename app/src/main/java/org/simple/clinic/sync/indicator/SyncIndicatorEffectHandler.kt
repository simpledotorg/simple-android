package org.simple.clinic.sync.indicator

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class SyncIndicatorEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: SyncIndicatorUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: SyncIndicatorUiActions): SyncIndicatorEffectHandler
  }

  fun build(): ObservableTransformer<SyncIndicatorEffect, SyncIndicatorEvent> =
      RxMobius
          .subtypeEffectHandler<SyncIndicatorEffect, SyncIndicatorEvent>()
          .build()
}
