package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class ConfirmRemovePrescriptionDialogEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): ConfirmRemovePrescriptionDialogEffectHandler
  }

  fun build(): ObservableTransformer<ConfirmRemovePrescriptionDialogEffect,
      ConfirmRemovePrescriptionDialogEvent> = RxMobius
      .subtypeEffectHandler<ConfirmRemovePrescriptionDialogEffect,
          ConfirmRemovePrescriptionDialogEvent>()
      .build()
}
