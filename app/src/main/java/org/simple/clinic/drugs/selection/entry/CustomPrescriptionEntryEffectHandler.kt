package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class CustomPrescriptionEntryEffectHandler @AssistedInject constructor(
    @Assisted val uiActions: CustomPrescriptionEntryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: CustomPrescriptionEntryUiActions): CustomPrescriptionEntryEffectHandler
  }

  fun build()
      : ObservableTransformer<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent>()
        .build()
  }
}
