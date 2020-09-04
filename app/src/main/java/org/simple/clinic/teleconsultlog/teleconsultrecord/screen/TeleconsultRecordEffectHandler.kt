package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class TeleconsultRecordEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): TeleconsultRecordEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultRecordEffect, TeleconsultRecordEvent> {
    return RxMobius.subtypeEffectHandler<TeleconsultRecordEffect, TeleconsultRecordEvent>()
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen)
        .build()
  }
}
