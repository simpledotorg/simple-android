package org.simple.clinic.sync.indicator

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class SyncIndicatorEffectHandler {
  private fun buildEffectHandler(): ObservableTransformer<SyncIndicatorEffect, SyncIndicatorEvent> =
      RxMobius
      .subtypeEffectHandler<SyncIndicatorEffect, SyncIndicatorEvent>()
      .build()

  companion object {
    fun create(): ObservableTransformer<SyncIndicatorEffect, SyncIndicatorEvent> =
        SyncIndicatorEffectHandler().buildEffectHandler()
  }
}
