package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class ConfirmRemoveBloodPressureEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: ConfirmRemoveBloodPressureDialogUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: ConfirmRemoveBloodPressureDialogUiActions): ConfirmRemoveBloodPressureEffectHandler
  }

  fun build(): ObservableTransformer<ConfirmRemoveBloodPressureEffect,
      ConfirmRemoveBloodPressureEvent> = RxMobius
      .subtypeEffectHandler<ConfirmRemoveBloodPressureEffect, ConfirmRemoveBloodPressureEvent>()
      .build()
}
