package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class CustomPrescriptionEntryEffectHandler(val uiActions: CustomPrescriptionEntryUiActions) {

  companion object {
    fun create(uiActions: CustomPrescriptionEntryUiActions): ObservableTransformer<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent> {
      return CustomPrescriptionEntryEffectHandler(uiActions).buildEffectHandler()
    }
  }

  private fun buildEffectHandler()
      : ObservableTransformer<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent>()
        .build()
  }
}
